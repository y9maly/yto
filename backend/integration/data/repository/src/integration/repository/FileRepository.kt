package integration.repository

import backend.core.types.*
import kotlin.time.Instant


interface FileRepository {
    suspend fun exists(id: FileId): Boolean

    suspend fun totalUserStorageUsed(client: ClientId): Long?

    suspend fun get(id: FileId): File?

    suspend fun getImageFile(file: FileId): ImageFile?

    suspend fun getFileTypes(file: FileId): FileTypes?

    suspend fun create(
        uri: String,
        name: String?,
        owner: FileOwner,
        uploadDate: Instant,
        expiresAt: Instant?,
        sizeBytes: Long,
        types: FileTypes,
    ): File
}
