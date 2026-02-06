@file:JvmName("ResultUserKt")

package y9to.api.types

import kotlinx.serialization.Serializable
import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName
import kotlin.time.Instant


typealias EditMeResult = Union<Unit, EditMeError>

@Serializable
sealed interface EditMeError {
    @Serializable
    data object Unauthenticated : EditMeError

    @Serializable
    data object NothingToChange : EditMeError

    @Serializable
    data class FieldErrors(
        val firstNameError: EditNameError?,
        val lastNameError: EditNameError?,
        val bioError: EditBioError?,
        val birthdayError: EditBirthdayError?,
        val coverError: EditCoverError?,
        val avatarError: EditAvatarError?,
    ) : EditMeError
}

@Serializable
sealed interface EditNameError {
    @Serializable
    data class FloodWait(val until: Instant?) : EditNameError // null if unknown
    @Serializable
    data object CannotBeBlank : EditNameError
    @Serializable
    data object ExceededLengthRange : EditNameError
}
@Serializable
sealed interface EditBioError {
    @Serializable
    data class FloodWait(val until: Instant?) : EditBioError // null if unknown
    @Serializable
    data object ExceededLengthRange : EditBioError
}

@Serializable
sealed interface EditBirthdayError {
    @Serializable
    data class FloodWait(val until: Instant?) : EditBirthdayError // null if unknown
    @Serializable
    data object ExceededDateRange : EditBirthdayError
}

@Serializable
sealed interface EditCoverError {
    @Serializable
    data object InvalidFile : EditCoverError
}

@Serializable
sealed interface EditAvatarError {
    @Serializable
    data object InvalidFile : EditAvatarError
}
