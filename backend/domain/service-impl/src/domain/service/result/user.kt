package domain.service.result

import domain.service.result.internals.mapBoth
import integration.repository.result.EditUserError as DbEditUserError
import integration.repository.result.EditUserOk as DbEditUserOk
import integration.repository.result.EditUserResult as DbEditUserResult


internal fun DbEditUserResult.map() = mapBoth({ map() }, { map() })
internal fun DbEditUserOk.map() = EditUserOk(new)
internal fun DbEditUserError.map() = when (this) {
    DbEditUserError.InvalidUserLink -> EditUserError.InvalidUserId
    DbEditUserError.InvalidCoverFileId -> EditUserError.FieldErrors(coverError = EditUserCoverError.InvalidFile)
    DbEditUserError.InvalidAvatarFileId -> EditUserError.FieldErrors(avatarError = EditUserAvatarError.InvalidFile)
}
