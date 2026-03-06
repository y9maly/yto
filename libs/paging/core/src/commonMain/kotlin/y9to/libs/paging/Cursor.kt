package y9to.libs.paging

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable(with = Cursor.Serializer::class)
data class Cursor(val payload: String) {
    object Serializer : KSerializer<Cursor> {
        private val underlying = String.serializer()
        override val descriptor = underlying.descriptor
        override fun serialize(encoder: Encoder, value: Cursor) = underlying.serialize(encoder, value.payload)
        override fun deserialize(decoder: Decoder) = Cursor(underlying.deserialize(decoder))
    }
}
