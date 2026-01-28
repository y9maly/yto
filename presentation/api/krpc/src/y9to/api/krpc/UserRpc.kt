package y9to.api.krpc

import kotlinx.rpc.annotations.Rpc
import y9to.api.types.EditMeResult
import y9to.api.types.InputUser
import y9to.api.types.MyProfile
import y9to.api.types.Token
import y9to.api.types.User
import y9to.common.types.Birthday
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none


@Rpc
interface UserRpc {
    suspend fun getMyProfile(token: Token): MyProfile? // null if unauthenticated
    suspend fun get(token: Token, input: InputUser): User?
    suspend fun editMe(
        token: Token,
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
    ): EditMeResult
}
