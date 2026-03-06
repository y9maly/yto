package y9to.libs.stdlib.serialization

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString


fun BinaryFormat.asHexStringFormat(): StringFormat {
    return HexStringFormat(this)
}

private class HexStringFormat(
    private val binaryFormat: BinaryFormat,
) : StringFormat {
    override val serializersModule = binaryFormat.serializersModule

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        return binaryFormat.encodeToHexString(serializer, value)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return binaryFormat.decodeFromHexString(deserializer, string)
    }
}
