package y9to.api.krpc

import kotlinx.rpc.annotations.Rpc
import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none


@Rpc
interface UserRpc {
    suspend fun getMyProfile(token: Token): MyProfile?

    suspend fun get(token: Token, input: InputUser): User?

    suspend fun editMe(
        token: Token,
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
        cover: Optional<FileId?> = none(),
        avatar: Optional<FileId?> = none(),
    ): EditMeResult
}
