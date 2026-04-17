package integration.repository.result

import backend.core.types.User
import y9to.libs.stdlib.Union


typealias EditUserResult = Union<EditUserOk, EditUserError>


data class EditUserOk(val new: User)


sealed interface EditUserError {
    data object InvalidUserLink : EditUserError
    data object InvalidCoverFileId : EditUserError
    data object InvalidAvatarFileId : EditUserError
}
