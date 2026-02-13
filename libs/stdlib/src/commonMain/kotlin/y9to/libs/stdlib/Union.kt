@file:OptIn(ExperimentalContracts::class)

package y9to.libs.stdlib

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.protobuf.ProtoNumber
import y9to.libs.stdlib.Union.IfErrorClause
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


// Юзаю пока не выкатят rich errors
@Serializable(Union.Serializer::class)
sealed class Union<out S, out E> {
    class Serializer<S, E>(
        val successSerializer: KSerializer<S>,
        val errorSerializer: KSerializer<E>,
    ) : KSerializer<Union<S, E>> {
        @OptIn(ExperimentalSerializationApi::class)
        override val descriptor = buildClassSerialDescriptor(
            serialName = "Union",
            successSerializer.descriptor,
            errorSerializer.descriptor
        ) {
            element("success", successSerializer.descriptor, listOf(ProtoNumber(1)), isOptional = true)
            element("error", successSerializer.descriptor, listOf(ProtoNumber(2)), isOptional = true)
        }

        @Suppress("UNCHECKED_CAST")
        override fun serialize(encoder: Encoder, value: Union<S, E>) {
            encoder.encodeStructure(descriptor) {
                if (value.isSuccess()) {
                    encodeSerializableElement(
                        descriptor,
                        descriptor.getElementIndex("success"),
                        successSerializer,
                        value.value as S
                    )
                } else {
                    encodeSerializableElement(
                        descriptor,
                        descriptor.getElementIndex("error"),
                        errorSerializer,
                        value.error as E
                    )
                }
            }
        }

        override fun deserialize(decoder: Decoder): Union<S, E> {
            return decoder.decodeStructure(descriptor) {
                val result = when (val index = decodeElementIndex(descriptor)) {
                    descriptor.getElementIndex("success") -> {
                        decodeSerializableElement(successSerializer.descriptor, index, successSerializer).asOk()
                    }

                    descriptor.getElementIndex("error") -> {
                        decodeSerializableElement(errorSerializer.descriptor, index, errorSerializer).asError()
                    }

                    else -> {
                        throw SerializationException("Unexpected index $index")
                    }
                }

                val index = decodeElementIndex(descriptor)
                if (index != DECODE_DONE)
                    throw SerializationException("Unexpected index $index")

                result
            }
        }
    }

    @Serializable(Success.Serializer::class)
    data class Success<out S>(val value: S) : Union<S, Nothing>() {
        class Serializer<S>(private val valueSerializer: KSerializer<S>) : KSerializer<Success<S>> {
            override val descriptor = valueSerializer.descriptor

            override fun serialize(encoder: Encoder, value: Success<S>) {
                valueSerializer.serialize(encoder, value.value)
            }

            override fun deserialize(decoder: Decoder): Success<S> {
                return valueSerializer.deserialize(decoder).asOk()
            }
        }
    }

    @Serializable(Error.Serializer::class)
    data class Error<out E>(val error: E) : Union<Nothing, E>() {
        class Serializer<E>(private val errorSerializer: KSerializer<E>) : KSerializer<Error<E>> {
            override val descriptor = errorSerializer.descriptor

            override fun serialize(encoder: Encoder, value: Error<E>) {
                errorSerializer.serialize(encoder, value.error)
            }

            override fun deserialize(decoder: Decoder): Error<E> {
                return errorSerializer.deserialize(decoder).asError()
            }
        }
    }

    fun isSuccess(): Boolean {
        contract {
            returns(true) implies (this@Union is Success)
            returns(false) implies (this@Union is Error)
        }

        return this is Success
    }

    fun isError(): Boolean {
        contract {
            returns(true) implies (this@Union is Error)
            returns(false) implies (this@Union is Success)
        }

        return this is Error
    }

    fun errorOrNull(): E? {
        return (this as? Error)?.error
    }

    fun orNull(): S? {
        return (this as? Success)?.value
    }

    inline fun onSuccess(block: (S) -> Unit): Union<S, E>  {
        if (isSuccess()) block(value)
        return this
    }

    inline fun onError(block: (E) -> Unit): Union<S, E> {
        if (isError()) block(error)
        return this
    }



    sealed class IfErrorClause<out E, SR> {
        class Error<out E, SR>(val value: E) : IfErrorClause<E, SR>()
        class Success<SR>(val value: SR) : IfErrorClause<Nothing, SR>()
    }
}

fun <S, E> Union<S, E>.successOrNull(): S? {
    if (isSuccess()) return value as S
    return null
}

fun <S, E> Union<S, E>.errorOrNull(): E? {
    if (isError()) return error as E
    return null
}

inline fun <S, E> Union<S, E>.successOrElse(block: (E) -> S): S {
    if (isSuccess()) return value as S
    return block(error as E)
}

inline fun <S, E> Union<S, E>.errorOrElse(block: (S) -> E): E {
    if (isError()) return error as E
    return block(value as S)
}

inline fun <S, E, NEW_S> Union<S, E>.mapSuccess(block: (S) -> NEW_S): Union<NEW_S, E> {
    if (isSuccess()) return block(value as S).asOk()
    return this
}

inline fun <S, E, NEW_E> Union<S, E>.mapError(block: (E) -> NEW_E): Union<S, NEW_E> {
    if (isError()) return block(error as E).asError()
    return this
}

fun <S> S.asOk(): Union.Success<S> = Union.Success(this)
fun <E> E.asError(): Union.Error<E> = Union.Error(this)

inline fun <S, E, SR> Union<S, E>.ifSuccess(block: (S) -> SR): IfErrorClause<E, SR> {
    return if (isSuccess())
        IfErrorClause.Success(block(value as S))
    else
        IfErrorClause.Error(error as E)
}

//inline fun <S, SR> Union.Success<S>.ifSuccess(block: (S) -> SR): SR {
//    return block(value)
//}

inline fun <S, SR> Union.Success<S>.ifSuccess(block: (S) -> SR): IfErrorClause.Success<SR> {
    return IfErrorClause.Success(block(value as S))
}

/**
 * warning: [block] was never called because union is always an Error
 */
inline fun <S, E, SR> Union.Error<S>.ifSuccess(block: (S) -> SR): IfErrorClause<E, Nothing> {
    return IfErrorClause.Error(error as E)
}

inline fun <E, SR : SER, ER : SER, SER> IfErrorClause<E, SR>.ifError(block: (E) -> ER): SER {
    return when (this) {
        is IfErrorClause.Success -> value
        is IfErrorClause.Error -> block(value)
    }
}

inline fun <SR> IfErrorClause.Success<SR>.ifError(block: (Nothing) -> Any? = {}): SR {
    return value
}
