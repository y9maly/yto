package y9to.api.controller

import presentation.integration.context.Context
import y9to.api.types.AuthState
import y9to.api.types.InputAuthMethod
import y9to.api.types.LogInResult
import y9to.api.types.LogOutResult
import y9to.api.types.Session
import y9to.api.types.Token


interface AuthController {
    suspend fun createSession(): Token

    // just until update system starts working
    context(_: Context) suspend fun needResetLocalCache(): Boolean

    context(_: Context) suspend fun getSession(): Session
    context(_: Context) suspend fun getAuthState(): AuthState
    context(_: Context) suspend fun logIn(method: InputAuthMethod): LogInResult
    context(_: Context) suspend fun logOut(): LogOutResult
}
