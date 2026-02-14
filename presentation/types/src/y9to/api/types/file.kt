package y9to.api.types

import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmInline
import kotlin.time.Instant


@S data class FileId(val long: Long)

@S data class File(
    val id: FileId,
    val name: String?,
    val uploadDate: Secure<Instant>,
    val sizeBytes: Long,
)

@S sealed interface FileSink {
    @S data class HttpOctetStream(val url: String) : FileSink
}

@S sealed interface FileSource {
    @S data class HttpOctetStream(val url: String) : FileSource
}

@S data class StorageQuota(
    val totalBytes: Long,
    val usedBytes: Long,
)


@S sealed interface FileType

@S data class FileTypes(
    val image: ImageFile?,
) {
    companion object {
        val Empty = FileTypes(null)
    }
}


@S data class ImageFile(
    val file: FileId,
    val format: ImageFileFormat,
    val width: Int?,
    val height: Int?,
) : FileType


@S data class ImageFileFormat(val name: String) {
    companion object
}

val ImageFileFormat.Companion.png get() = ImageFileFormat("png")
val ImageFileFormat.Companion.jpeg get() = ImageFileFormat("jpeg")
val ImageFileFormat.Companion.webp get() = ImageFileFormat("webp")
val ImageFileFormat.isPng get() = this == ImageFileFormat.png
val ImageFileFormat.isJpeg get() = this == ImageFileFormat.jpeg
val ImageFileFormat.isWebp get() = this == ImageFileFormat.webp
