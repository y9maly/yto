@file:JvmName("ResultFileKt")

package y9to.api.types

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName


@Serializable
sealed interface UploadFileError {
    @Serializable
    data object StorageQuotaExceeded : UploadFileError
}
