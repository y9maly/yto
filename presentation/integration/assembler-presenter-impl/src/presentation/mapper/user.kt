package presentation.mapper

import y9to.api.types.User
import y9to.api.types.UserId
import y9to.api.types.UserPreview
import backend.core.types.User as BackendUser
import backend.core.types.UserId as BackendUserId
import backend.core.types.UserPreview as BackendUserPreview


fun BackendUserId.map() = UserId(long)
fun UserId.map() = BackendUserId(long)


fun BackendUser.map(
    showPhoneNumber: Boolean,
    showEmail: Boolean,
) = User(
    id = id.map(),
    registrationDate = registrationDate,
    firstName = firstName,
    lastName = lastName,
    phoneNumber = if (showPhoneNumber) phoneNumber else null,
    email = if (showEmail) email else null,
    bio = bio,
    birthday = birthday,
    cover = cover?.map(),
    avatar = avatar?.map(),
)

fun BackendUserPreview.map() = UserPreview(
    id = id.map(),
    firstName = firstName,
    lastName = lastName,
)
