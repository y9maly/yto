package y9to.libs.paging

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


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

