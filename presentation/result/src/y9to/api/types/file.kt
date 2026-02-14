@file:JvmName("ResultFileKt")

package y9to.api.types

import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmName


@S sealed interface UploadFileError {
    @S data object StorageQuotaExceeded : UploadFileError
}
