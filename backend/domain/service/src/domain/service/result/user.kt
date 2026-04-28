package domain.service.result

import backend.core.types.User
import y9to.libs.stdlib.Union


typealias RegisterUserResult = Union<User, RegisterUserError>
typealias EditUserResult = Union<EditUserOk, EditUserError>


data class EditUserOk(val new: User)


sealed interface RegisterUserError {
    data object PhoneNumberConflict : RegisterUserError
    data object EmailConflict : RegisterUserError
}

sealed interface EditUserError {
    data object InvalidUserId : EditUserError

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
