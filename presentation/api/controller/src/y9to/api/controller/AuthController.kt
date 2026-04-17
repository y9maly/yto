package y9to.api.controller

import backend.core.types.SessionId
import presentation.integration.context.Context
import y9to.api.types.AuthState
import y9to.api.types.InputAuthMethod
import y9to.api.types.LogInResult
import y9to.api.types.LogOutResult
import y9to.api.types.RefreshToken
import y9to.api.types.Session
import y9to.api.types.Token


interface AuthController {
    suspend fun createSession(): SessionId

    context(_: Context) suspend fun getSession(): Session
    context(_: Context) suspend fun getAuthState(): AuthState
    context(_: Context) suspend fun logIn(method: InputAuthMethod): LogInResult
    context(_: Context) suspend fun logOut(): LogOutResult
}
