package domain.service.result

import backend.core.types.File


sealed interface UploadFilePartsResult {
    data class Ok(val uri: String) : UploadFilePartsResult
    data object InvalidFileOwner : UploadFilePartsResult
    data object OwnerStorageQuotaExceeded : UploadFilePartsResult
}

sealed interface UploadFilePartResult {
    data class Ok(val newSizeBytes: Long) : UploadFilePartResult
    data object InvalidURI : UploadFilePartResult
    data object OwnerStorageQuotaExceeded : UploadFilePartResult
}

sealed interface CommitFilePartsResult {
    data class Ok(val file: File) : CommitFilePartsResult
    data object InvalidURI : CommitFilePartsResult
    data object InvalidFileOwner : CommitFilePartsResult
    data object OwnerStorageQuotaExceeded : CommitFilePartsResult
}
