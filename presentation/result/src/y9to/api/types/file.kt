@file:JvmName("ResultFileKt")

package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmName


@S sealed interface UploadFileError {
    @SerialName("StorageQuotaExceeded")
    @S data object StorageQuotaExceeded : UploadFileError

    @SerialName("UnknownError")
    @S data class UnknownError(val message: String?) : UploadFileError
}
