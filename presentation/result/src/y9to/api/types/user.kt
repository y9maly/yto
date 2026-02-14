@file:JvmName("ResultUserKt")

package y9to.api.types

import kotlinx.serialization.Serializable as S
import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName
import kotlin.time.Instant


typealias EditMeResult = Union<Unit, EditMeError>

@S sealed interface EditMeError {
    @S data object Unauthenticated : EditMeError

    @S data object NothingToChange : EditMeError

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
    @S data class FloodWait(val until: Instant?) : EditNameError // null if unknown
    @S data object CannotBeBlank : EditNameError
    @S data object ExceededLengthRange : EditNameError
}
@S sealed interface EditBioError {
    @S data class FloodWait(val until: Instant?) : EditBioError // null if unknown
    @S data object ExceededLengthRange : EditBioError
}

@S sealed interface EditBirthdayError {
    @S data class FloodWait(val until: Instant?) : EditBirthdayError // null if unknown
    @S data object ExceededDateRange : EditBirthdayError
}

@S sealed interface EditCoverError {
    @S data object InvalidFile : EditCoverError
}

@S sealed interface EditAvatarError {
    @S data object InvalidFile : EditAvatarError
}
