package y9to.api.y9to.api.endpoint

import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none


interface UserEndpoint {
    fun getMyProfilee(): MyProfile? // null if unauthenticated

    fun get(input: InputUser): User?

    fun editMe(
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
        cover: Optional<FileId?> = none(),
        avatar: Optional<FileId?> = none(),
    ): EditMeResult
}
