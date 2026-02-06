package domain.service.result

import backend.core.types.User
import domain.service.result.internals.mapBoth
import integration.repository.result.UpdateUserError
import integration.repository.result.UpdateUserOk
import integration.repository.result.UpdateUserResult
import y9to.libs.stdlib.Union


typealias EditUserResult = Union<EditUserOk, EditUserError>


data class EditUserOk(val new: User)


sealed interface EditUserError {
    data object UnknownUserReference : EditUserError

    data class FieldErrors(
        val firstNameError: EditUserNameError? = null,
        val lastNameError: EditUserNameError? = null,
        val bioError: EditUserBioError? = null,
        val birthdayError: EditUserBirthdayError? = null,
        val coverError: EditUserCoverError? = null,
        val avatarError: EditUserAvatarError? = null,
    ) : EditUserError
}

sealed interface EditUserNameError {
    data object CannotBeBlank : EditUserNameError
    data object ExceededLengthRange : EditUserNameError
}

sealed interface EditUserBioError {
    data object ExceededLengthRange : EditUserBioError
}

sealed interface EditUserBirthdayError {
    data object ExceededDateRange : EditUserBirthdayError
}

sealed interface EditUserCoverError {
    data object InvalidFile : EditUserCoverError
}

sealed interface EditUserAvatarError {
    data object InvalidFile : EditUserAvatarError
}


fun UpdateUserResult.map() = mapBoth({ map() }, { map() })
fun UpdateUserOk.map() = EditUserOk(new)
fun UpdateUserError.map() = when (this) {
    UpdateUserError.UnknownUserId -> EditUserError.UnknownUserReference
    UpdateUserError.InvalidCoverFileId -> EditUserError.FieldErrors(coverError = EditUserCoverError.InvalidFile)
    UpdateUserError.InvalidAvatarFileId -> EditUserError.FieldErrors(avatarError = EditUserAvatarError.InvalidFile)
}
