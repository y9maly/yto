package y9to.api.controller

import presentation.integration.context.Context
import y9to.api.types.EditMeResult
import y9to.api.types.FileId
import y9to.api.types.InputUser
import y9to.api.types.MyProfile
import y9to.api.types.Token
import y9to.api.types.User
import y9to.common.types.Birthday
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none


interface UserController {
    context(_: Context) suspend fun getMyProfile(): MyProfile? // null if unauthenticated

    context(_: Context) suspend fun get(input: InputUser): User?

    context(_: Context) suspend fun editMe(
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
        cover: Optional<FileId?> = none(),
        avatar: Optional<FileId?> = none(),
    ): EditMeResult
}
