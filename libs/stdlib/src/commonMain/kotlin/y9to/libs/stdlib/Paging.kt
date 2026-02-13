package y9to.libs.stdlib

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


// todo move outside stdlib


@Serializable
data class Slice<out T>(val list: List<T>, val nextPagingKey: PagingKey?)

@Serializable
data class Slice2D<out T>(val previousPagingKey: PagingKey?, val list: List<T>, val nextPagingKey: PagingKey?)


@Serializable
sealed interface SpliceKey<out PAGING_OPTIONS> {
    @Serializable
    data class Initialize<out PAGING_OPTIONS>(val options: PAGING_OPTIONS) : SpliceKey<PAGING_OPTIONS>

    @Serializable
    data class Continue(val pagingKey: PagingKey) : SpliceKey<Nothing>

    fun <R> fold(
        initialize: (PAGING_OPTIONS) -> R,
        next: (pagingKey: PagingKey) -> R,
    ): R = when (this) {
        is Continue -> next(pagingKey)
        is Initialize -> initialize(options)
    }
}

inline fun <T, R> SpliceKey<T>.mapOptions(transform: (T) -> R): SpliceKey<R> = when (this) {
    is SpliceKey.Continue -> this
    is SpliceKey.Initialize -> SpliceKey.Initialize(transform(options))
}


@Serializable(with = PagingKey.Serializer::class)
interface PagingKey {
    object Serializer : KSerializer<PagingKey> {
        override val descriptor = PrimitiveSerialDescriptor("PagingKey", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: PagingKey) {
            if (value !is SerializablePagingKey)
                error("This PagingKey is not serializable: $value")
            encoder.encodeString(value.serialize().string)
        }

        override fun deserialize(decoder: Decoder): PagingKey {
            return SerializedPagingKey(decoder.decodeString())
        }
    }
}


fun PagingKey.trySerialize(): SerializedPagingKey? =
    if (this is SerializablePagingKey) serialize()
    else null

fun PagingKey.serializeOrThrow(): SerializedPagingKey =
    trySerialize() ?: error("$this must be serializable, but it doesn't.")

interface SerializablePagingKey : PagingKey {
    fun serialize(): SerializedPagingKey
}

data class SerializedPagingKey(val string: String) : SerializablePagingKey {
    override fun serialize() = this
}






inline fun <T, R> Slice<T>.mapList(transform: (T) -> R): Slice<R> =
    Slice(list.map(transform), nextPagingKey)

inline fun <T> Slice<T>.mapNextPagingKey(transform: (PagingKey) -> PagingKey): Slice<T> =
    Slice(list, nextPagingKey?.let(transform))
