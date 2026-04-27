package presentation.api.krpc

import presentation.api.krpc.internals.authenticate
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import presentation.tokenProvider.RefreshTokensResult
import presentation.tokenProvider.TokenProvider
import y9to.api.controller.AuthController
import y9to.api.krpc.AuthRpc
import y9to.api.types.InputAuthMethod
import y9to.api.types.RefreshToken
import y9to.api.types.Token


class AuthRpcDefault(
    private val authenticator: Authenticator,
    private val tokenProvider: TokenProvider,
    private val controller: AuthController
) : AuthRpc {
    override suspend fun createSession(): Pair<RefreshToken, Token> {
        val sessionId = controller.createSession()
        val (refreshToken, accessToken) = tokenProvider.issueTokens(forSession = sessionId)
            ?: error("Must be unreachable because session ${sessionId.long} must be exists")
        return refreshToken to accessToken
    }

    override suspend fun refreshTokens(refreshToken: RefreshToken): Pair<RefreshToken, Token>? {
        return when (val result = tokenProvider.refreshTokens(refreshToken)) {
            is RefreshTokensResult.Ok -> result.refreshToken to result.accessToken
            RefreshTokensResult.ExpiredRefreshToken -> null
            RefreshTokensResult.InvalidRefreshToken -> null
        }
    }

    override suspend fun getSession(token: Token) =
        authenticate(token) { getSession() }

    override suspend fun getAuthState(token: Token) =
        authenticate(token) { getAuthState() }

    override suspend fun getLoginState(token: Token) =
        authenticate(token) { getLoginState() }

    override suspend fun logIn(token: Token, method: InputAuthMethod) =
        authenticate(token) { logIn(method) }

    override suspend fun logOut(token: Token) =
        authenticate(token) { logOut() }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context) AuthController.() -> R) =
        authenticate(authenticator, token) { block(this, controller) }
}
