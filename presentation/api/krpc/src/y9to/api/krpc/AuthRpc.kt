package y9to.api.krpc

import kotlinx.rpc.annotations.Rpc
import y9to.api.types.AuthState
import y9to.api.types.InputAuthMethod
import y9to.api.types.LogInResult
import y9to.api.types.LogOutResult
import y9to.api.types.Session
import y9to.api.types.Token
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.successOrElse


@Rpc
interface AuthRpc {
    suspend fun createSession(): Token
    suspend fun needResetLocalCache(token: Token): Boolean
    suspend fun getSession(token: Token): Session
    suspend fun getAuthState(token: Token): AuthState
    suspend fun logIn(token: Token, method: InputAuthMethod): LogInResult
    suspend fun logOut(token: Token): LogOutResult
}
