package y9to.api.krpc.types

import kotlinx.serialization.Serializable


@Serializable
sealed interface FileSink {
    @Serializable
    data class HttpOctetStream(val url: String) : FileSink
}

@Serializable
sealed interface FileSource {
    @Serializable
    data class HttpOctetStream(val url: String) : FileSource
}
