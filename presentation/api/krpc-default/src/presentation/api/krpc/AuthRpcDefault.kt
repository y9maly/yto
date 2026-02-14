package presentation.api.krpc

import presentation.api.krpc.internals.authenticate
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import y9to.api.controller.AuthController
import y9to.api.krpc.AuthRpc
import y9to.api.types.InputAuthMethod
import y9to.api.types.Token


class AuthRpcDefault(
    private val authenticator: Authenticator,
    private val controller: AuthController
) : AuthRpc {
    override suspend fun createSession(): Token {
        return controller.createSession()
    }

    override suspend fun needResetLocalCache(token: Token) =
        authenticate(token) { needResetLocalCache() }

    override suspend fun getSession(token: Token) =
        authenticate(token) { getSession() }

    override suspend fun getAuthState(token: Token) =
        authenticate(token) { getAuthState() }

    override suspend fun logIn(token: Token, method: InputAuthMethod) =
        authenticate(token) { logIn(method) }

    override suspend fun logOut(token: Token) =
        authenticate(token) { logOut() }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context) AuthController.() -> R) =
        authenticate(authenticator, token) { block(this, controller) }
}
