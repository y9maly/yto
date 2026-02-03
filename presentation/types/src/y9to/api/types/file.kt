package y9to.api.types

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant


@Serializable
data class FileId(val long: Long)

@Serializable
data class File(
    val id: FileId,
    val name: String?,
    val uploadDate: Secure<Instant>,
    val sizeBytes: Long,
)

@Serializable
data class StorageQuota(
    val totalBytes: Long,
    val usedBytes: Long,
)


@Serializable
sealed interface FileType

@Serializable
data class FileTypes(
    val image: ImageFile?,
) {
    companion object {
        val Empty = FileTypes(null)
    }
}


@Serializable
data class ImageFile(
    val file: FileId,
    val format: ImageFileFormat,
    val width: Int?,
    val height: Int?,
) : FileType


@Serializable
data class ImageFileFormat(val name: String) {
    companion object
}

val ImageFileFormat.Companion.png get() = ImageFileFormat("png")
val ImageFileFormat.Companion.jpeg get() = ImageFileFormat("jpeg")
val ImageFileFormat.Companion.webp get() = ImageFileFormat("webp")
val ImageFileFormat.isPng get() = this == ImageFileFormat.png
val ImageFileFormat.isJpeg get() = this == ImageFileFormat.jpeg
val ImageFileFormat.isWebp get() = this == ImageFileFormat.webp
