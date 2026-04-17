package domain.service

import backend.core.types.*
import domain.service.result.CommitFilePartsResult
import domain.service.result.UploadFilePartResult
import domain.service.result.UploadFilePartsResult
import kotlinx.io.Source


interface FileService {
    suspend fun get(id: FileId): File?

    suspend fun getClientStorageQuota(client: ClientId): ClientStorageQuota?

    suspend fun uploadParts(
        name: String?,
        owner: FileOwner,
        expectedSize: Long?, // null if unknown
        types: FileTypes?,
    ): UploadFilePartsResult

    suspend fun uploadPart(
        uri: String,
        bytes: Source,
    ): UploadFilePartResult

    suspend fun commitParts(
        uri: String,
        types: FileTypes?,
    ): CommitFilePartsResult

    suspend fun upload(
        name: String?,
        owner: FileOwner,
        types: FileTypes,
        bytes: Source,
    ): File

    suspend fun download(id: FileId): Source?
    suspend fun download(uri: String): Source?

    suspend fun create(
        name: String?,
        uri: String,
        owner: FileOwner,
        sizeBytes: Long,
        types: FileTypes,
    ): File
}
