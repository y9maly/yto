@file:JvmName("ResultUserKt")

package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName
import kotlin.time.Instant


typealias EditMeResult = Union<Unit, EditMeError>

@S sealed interface EditMeError {
    @SerialName("Unauthenticated")
    @S data object Unauthenticated : EditMeError

    @SerialName("NothingToChange")
    @S data object NothingToChange : EditMeError

    @SerialName("FieldErrors")
    @S data class FieldErrors(
        val firstNameError: EditNameError?,
        val lastNameError: EditNameError?,
        val bioError: EditBioError?,
        val birthdayError: EditBirthdayError?,
        val coverError: EditCoverError?,
        val avatarError: EditAvatarError?,
    ) : EditMeError
}

@S sealed interface EditNameError {
    @SerialName("FloodWait")
    @S data class FloodWait(val until: Instant?) : EditNameError // null if unknown

    @SerialName("CannotBeBlank")
    @S data object CannotBeBlank : EditNameError

    @SerialName("ExceededLengthRange")
    @S data object ExceededLengthRange : EditNameError
}

@S sealed interface EditBioError {
    @SerialName("FloodWait")
    @S data class FloodWait(val until: Instant?) : EditBioError // null if unknown

    @SerialName("ExceededLengthRange")
    @S data object ExceededLengthRange : EditBioError
}

@S sealed interface EditBirthdayError {
    @SerialName("FloodWait")
    @S data class FloodWait(val until: Instant?) : EditBirthdayError // null if unknown

    @SerialName("ExceededDateRange")
    @S data object ExceededDateRange : EditBirthdayError
}

@S sealed interface EditCoverError {
    @SerialName("InvalidFile")
    @S data object InvalidFile : EditCoverError

    @SerialName("FileTooBigForCover")
    @S data object FileTooBigForCover : EditCoverError

    @SerialName("UnsupportedFileFormatForCover")
    @S data object UnsupportedFileFormatForCover : EditCoverError
}

@S sealed interface EditAvatarError {
    @SerialName("InvalidFile")
    @S data object InvalidFile : EditAvatarError

    @SerialName("FileTooBigForAvatar")
    @S data object FileTooBigForAvatar : EditAvatarError

    @SerialName("UnsupportedFileFormatForAvatar")
    @S data object UnsupportedFileFormatForAvatar : EditAvatarError
}
