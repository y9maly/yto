package backend.core.types

import kotlin.time.Instant


@JvmInline
value class FileId(val long: Long)

data class File(
    val id: FileId,
    val uri: String,
    val name: String?,
    val uploadDate: Instant,
    val expiresAt: Instant?,
    val sizeBytes: Long,
    val owner: FileOwner,
)

sealed interface FileOwner {
    data object Unknown : FileOwner
    data class User(val user: UserId, val session: SessionId?) : FileOwner
}

data class ClientStorageQuota(
    val totalBytes: Long,
    val usedBytes: Long,
) {
    val remainingBytes get() = (totalBytes - usedBytes).coerceAtLeast(0)
}


sealed interface FileType

data class FileTypes(
    val image: ImageFile?,
) {
    companion object {
        val Empty = FileTypes(null)
    }
}


data class ImageFile(
    val file: FileId,
    val format: ImageFileFormat,
    val width: Int?,
    val height: Int?,
) : FileType


@JvmInline
value class ImageFileFormat(val name: String) {
    companion object
}

val ImageFileFormat.Companion.png get() = ImageFileFormat("png")
val ImageFileFormat.Companion.jpeg get() = ImageFileFormat("jpeg")
val ImageFileFormat.Companion.webp get() = ImageFileFormat("webp")
val ImageFileFormat.isPng get() = this == ImageFileFormat.png
val ImageFileFormat.isJpeg get() = this == ImageFileFormat.jpeg
val ImageFileFormat.isWebp get() = this == ImageFileFormat.webp
