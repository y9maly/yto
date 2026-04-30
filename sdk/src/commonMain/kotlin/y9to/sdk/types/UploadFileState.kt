package y9to.sdk.types

import y9to.api.types.File
import y9to.api.types.UploadFileError


sealed interface UploadFileState {
    sealed interface InProgress : UploadFileState {
        val uploadedBytes: Long
        val totalBytes: Long?
    }
    data class Connecting(override val uploadedBytes: Long, override val totalBytes: Long?) : InProgress
    data class Uploading(override val uploadedBytes: Long, override val totalBytes: Long?) : InProgress

    data class Done(val file: File) : UploadFileState

    data class Cancelled(val uploadedBytes: Long, val totalBytes: Long?) : UploadFileState
    data class Error(val error: UploadFileError) : UploadFileState
    data class Exception(val throwable: Throwable) : UploadFileState
}
