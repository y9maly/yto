package backend.core.types

import kotlin.time.Instant
import kotlinx.serialization.Serializable as S


@JvmInline
@S value class FileId(val long: Long)

@S data class File(
    val id: FileId,
    val uri: String,
    val name: String?,
    val uploadDate: Instant,
    val expiresAt: Instant?,
    val sizeBytes: Long,
    val owner: FileOwner,
)

@S sealed interface FileOwner {
    @S data object Unknown : FileOwner
    @S data class User(val user: UserId, val session: SessionId?) : FileOwner
}

@S data class ClientStorageQuota(
    val totalBytes: Long,
    val usedBytes: Long,
) {
    val remainingBytes get() = (totalBytes - usedBytes).coerceAtLeast(0)
}


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


@JvmInline
@S value class ImageFileFormat(val name: String) {
    companion object
}

val ImageFileFormat.Companion.png get() = ImageFileFormat("png")
val ImageFileFormat.Companion.jpeg get() = ImageFileFormat("jpeg")
val ImageFileFormat.Companion.webp get() = ImageFileFormat("webp")
val ImageFileFormat.isPng get() = this == ImageFileFormat.png
val ImageFileFormat.isJpeg get() = this == ImageFileFormat.jpeg
val ImageFileFormat.isWebp get() = this == ImageFileFormat.webp
