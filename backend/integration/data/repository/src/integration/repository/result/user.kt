package integration.repository.result

import backend.core.types.User
import y9to.libs.stdlib.Union


typealias UpdateUserResult = Union<UpdateUserOk, UpdateUserError>


data class UpdateUserOk(val new: User)


sealed interface UpdateUserError {
    data object UnknownUserId : UpdateUserError
}
