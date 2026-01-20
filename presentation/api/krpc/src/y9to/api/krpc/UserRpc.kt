package y9to.api.krpc

import kotlinx.rpc.annotations.Rpc
import y9to.api.types.InputUser
import y9to.api.types.Token
import y9to.api.types.User


@Rpc
interface UserRpc {
    suspend fun get(token: Token, input: InputUser): User?
}
