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
}


fun UpdateUserResult.map() = mapBoth({ map() }, { map() })
fun UpdateUserOk.map() = EditUserOk(new)
fun UpdateUserError.map() = when (this) {
    UpdateUserError.UnknownUserId -> EditUserError.UnknownUserReference
}
