@file:Suppress("unused", "NOTHING_TO_INLINE", "PropertyName")
@file:OptIn(ExperimentalContracts::class)

package y9to.libs.stdlib.optional

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.js.JsName
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

class NonePointerException : RuntimeException()

@Serializable(Optional.Serializer::class)
@Suppress("UNCHECKED_CAST")
@JvmInline
value class Optional<out T> @PublishedApi internal constructor(
    @PublishedApi
    internal val value: Any?
) {
    class Serializer<T>(val tSerializer: KSerializer<T>) : KSerializer<Optional<T>> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
            "Optional",
            tSerializer.descriptor
        ) {
            element("value", tSerializer.descriptor, isOptional = true)
        }

        override fun serialize(encoder: Encoder, value: Optional<T>) {
            encoder.encodeStructure(descriptor) {
                if (value.isPresent) {
                    encodeSerializableElement(descriptor, descriptor.getElementIndex("value"), tSerializer, value.value as T)
                }
            }
        }

        override fun deserialize(decoder: Decoder): Optional<T> = decoder.decodeStructure(descriptor) {
            var decoded = false
            var value: T? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    descriptor.getElementIndex("value") -> {
                        decoded = true
                        value = decodeSerializableElement(descriptor, index, tSerializer)
                    }

                    CompositeDecoder.DECODE_DONE -> break

                    else -> error("Unexpected index: $index")
                }
            }

            if (decoded)
                return@decodeStructure present(value as T)
            return@decodeStructure none()
        }
    }

    @PublishedApi
    internal object NONE

    companion object {
        @JvmStatic
        @PublishedApi
        internal val none = Optional<Nothing>(NONE)
    }

    // todo present/none?
    inline val isPresent get() = NONE != value
    inline val isNone get() = NONE == value

    fun getOrThrow(): T = if (isPresent) value as T else throw NonePointerException()
    fun getOrNull(): T? = if (isPresent) value as T else null
}

// Builders

inline fun <T> present(value: T) = Optional<T>(value)
inline fun <T : Any> presentIfNotNull(value: T?) = Optional<T>(value)
inline fun <T> none(): Optional<T> = Optional.none
@JvmName("noneAny")
@JsName("noneAny")
inline fun none() = none<Nothing>()

//

inline fun <R> Optional<R>.onPresent(block: (R) -> Unit): Optional<R> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (isPresent) block(getOrThrow())
    return this
}

inline fun <R> Optional<R>.onNone(block: () -> Unit): Optional<R> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (isNone) block()
    return this
}

inline fun <T, R> Optional<T>.fold(onPresent: (T) -> R, onNone: () -> R): R {
    contract {
        callsInPlace(onPresent, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onNone, InvocationKind.AT_MOST_ONCE)
    }
    return if (isPresent) onPresent(getOrThrow()) else onNone()
}

inline fun <T : R, R> Optional<T>.getOrDefault(default: R): R {
    return if (isPresent) getOrThrow() else default
}


inline fun <T : R, R> Optional<T>.getOrElse(onNone: () -> R): R {
    contract { callsInPlace(onNone, InvocationKind.AT_MOST_ONCE) }
    return if (isPresent) getOrThrow() else onNone()
}

inline fun <T, R> Optional<T>.map(transform: (T) -> R): Optional<R> {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return if (isPresent) present(transform(getOrThrow())) else none()
}

inline fun <T : R, R> Optional<T>.recover(onNone: () -> R): Optional<R> {
    contract { callsInPlace(onNone, InvocationKind.AT_MOST_ONCE) }
    return if (isPresent) this else present(onNone())
}

inline fun <T : R, R> Optional<T>.tryRecover(onNone: () -> Optional<R>): Optional<R> {
    contract { callsInPlace(onNone, InvocationKind.AT_MOST_ONCE) }
    return if (isPresent) this else onNone()
}

// Integration with kotlin.Result

inline fun <T> Result<T>.getOrNone(): Optional<T> = fold(
    onSuccess = { present(it) },
    onFailure = { none() }
)
