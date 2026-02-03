package domain.service

import backend.core.types.ClientId
import backend.core.types.ClientStorageQuota
import backend.core.types.File
import backend.core.types.FileId
import backend.core.types.FileOwner
import backend.core.types.FileTypes
import domain.service.result.CommitFilePartsResult
import domain.service.result.UploadFilePartResult
import domain.service.result.UploadFilePartsResult
import integration.fileStorage.AppendResult
import integration.fileStorage.CloseResult
import integration.fileStorage.CreateResult
import integration.fileStorage.FileStorage
import integration.repository.MainRepository
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.buffered
import y9to.libs.stdlib.InterfaceClass
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Instant


class FileService @InterfaceClass constructor(
    private val repo: MainRepository,
    private val fileStorage: FileStorage,
    private val clock: Clock,
) {
    private class UploadingFile(
        val startDate: Instant,
        var lastDate: Instant,
        val name: String?,
        val owner: FileOwner,
        val types: FileTypes?,
    )

    private val uploadingFiles = ConcurrentHashMap<String, UploadingFile>()

    suspend fun get(id: FileId): File? {
        return repo.file.select(id)
    }

    suspend fun getClientStorageQuota(client: ClientId): ClientStorageQuota? {
        val total = 5L * 1024L * 1024L * 1024L // 5gb
        val used = repo.file.totalUserStorageUsed(client) ?: return null
        return ClientStorageQuota(totalBytes = total, usedBytes = used)
    }

    suspend fun uploadParts(
        name: String?,
        owner: FileOwner,
        expectedSize: Long?, // null if unknown
        types: FileTypes?,
    ): UploadFilePartsResult {
        if (expectedSize != null) {
            when (owner) {
                is FileOwner.User -> {
                    val storageQuota = getClientStorageQuota(owner.user)
                        ?: return UploadFilePartsResult.InvalidFileOwner
                    if (storageQuota.remainingBytes < expectedSize)
                        return UploadFilePartsResult.OwnerStorageQuotaExceeded
                }

                is FileOwner.Unknown -> { /* do nothing */ }
            }
        }

        val uri = when (
            val result = fileStorage.createAndClose(null)
        ) {
            is CreateResult.Ok -> result.uri
        }

        val now = clock.now()
        uploadingFiles[uri] = UploadingFile(
            startDate = now,
            lastDate = now,
            name = name,
            owner = owner,
            types = types,
        )

        return UploadFilePartsResult.Ok(uri)
    }

    suspend fun uploadPart(
        uri: String,
        bytes: Source,
    ): UploadFilePartResult {
        val uploadingFile = uploadingFiles[uri]
            ?: return UploadFilePartResult.InvalidURI
        uploadingFile.lastDate = clock.now()

        return when (
            val result = fileStorage.append(uri, bytes)
        ) {
            is AppendResult.Ok -> UploadFilePartResult.Ok(result.newSizeBytes)
            is AppendResult.InvalidURI -> UploadFilePartResult.InvalidURI
        }
    }

    suspend fun commitParts(
        uri: String,
        types: FileTypes?,
    ): CommitFilePartsResult {
        val uploadingFile = uploadingFiles.remove(uri)
            ?: return CommitFilePartsResult.InvalidURI

        val sizeBytes = when (val result = fileStorage.close(uri)) {
            is CloseResult.Ok -> result.totalSizeBytes
            is CloseResult.InvalidURI -> return CommitFilePartsResult.InvalidURI
        }

        // check storage quota
        when (val owner = uploadingFile.owner) {
            is FileOwner.User -> {
                val storageQuota = getClientStorageQuota(owner.user)

                if (storageQuota == null) {
                    fileStorage.delete(uri)
                    return CommitFilePartsResult.InvalidFileOwner
                }

                if (storageQuota.remainingBytes < sizeBytes) {
                    fileStorage.delete(uri)
                    return CommitFilePartsResult.OwnerStorageQuotaExceeded
                }
            }

            is FileOwner.Unknown -> { /* do nothing */ }
        }

        val file = repo.file.insert(
            uri = uri,
            name = uploadingFile.name,
            owner = uploadingFile.owner,
            sizeBytes = sizeBytes,
            uploadDate = clock.now(),
            expiresAt = null,
            types = types ?: uploadingFile.types ?: FileTypes.Empty,
        )

        return CommitFilePartsResult.Ok(file)
    }

    suspend fun upload(
        name: String?,
        owner: FileOwner,
        types: FileTypes,
        bytes: Source,
    ): File {
        val (uri, sizeBytes) = when (val result = fileStorage.createAndClose(bytes)) {
            is CreateResult.Ok -> result.uri to result.sizeBytes
        }

        return repo.file.insert(
            uri = uri,
            name = name,
            owner = owner,
            sizeBytes = sizeBytes,
            uploadDate = clock.now(),
            expiresAt = null,
            types = types,
        )
    }

    suspend fun download(id: FileId): Source? {
        return download(repo.file.select(id)?.uri ?: return null)
    }

    suspend fun download(uri: String): Source? {
        return fileStorage.readFile(uri)
    }

    suspend fun create(
        name: String?,
        uri: String,
        owner: FileOwner,
        sizeBytes: Long,
        types: FileTypes,
    ): File {
        return repo.file.insert(
            uri = uri,
            name = name,
            owner = owner,
            uploadDate = clock.now(),
            expiresAt = null,
            sizeBytes = sizeBytes,
            types = types,
        )
    }
}
