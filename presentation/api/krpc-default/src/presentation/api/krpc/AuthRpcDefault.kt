package presentation.api.krpc

import presentation.api.krpc.internals.authenticate
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.tokenProvider.RefreshTokensResult
import presentation.tokenProvider.TokenProvider
import y9to.api.controller.AuthController
import y9to.api.krpc.AuthRpc
import y9to.api.types.AuthState
import y9to.api.types.CheckConfirmCodeResult
import y9to.api.types.CheckOAuthResult
import y9to.api.types.CheckPassword2FAResult
import y9to.api.types.FileId
import y9to.api.types.InputAuthMethod
import y9to.api.types.LogOutResult
import y9to.api.types.LoginState
import y9to.api.types.RefreshToken
import y9to.api.types.RegisterResult
import y9to.api.types.Session
import y9to.api.types.StartLoginWithEmailResult
import y9to.api.types.StartLoginWithPhoneNumberResult
import y9to.api.types.StartLoginWithTelegramOAuthResult
import y9to.api.types.Token
import y9to.common.types.Birthday


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

    override suspend fun logOut(token: Token) =
        authenticate(token) { logOut() }

    override suspend fun getLoginState(token: Token) =
        authenticate(token) { getLoginState() }

    override suspend fun startLoginWithPhoneNumber(token: Token, phoneNumber: String) =
        authenticate(token) { startLoginWithPhoneNumber(phoneNumber) }

    override suspend fun startLoginWithEmail(token: Token, email: String) =
        authenticate(token) { startLoginWithEmail(email) }

    override suspend fun startLoginWithTelegramOAuth(token: Token, requestPhoneNumber: Boolean) =
        authenticate(token) { startLoginWithTelegramOAuth(requestPhoneNumber) }

    override suspend fun checkConfirmCode(token: Token, code: String) =
        authenticate(token) { checkConfirmCode(code) }

    override suspend fun checkPassword2FA(token: Token, password: String) =
        authenticate(token) { checkPassword2FA(password) }

    override suspend fun checkOAuth(token: Token, authorizationCode: String, authorizationState: String) =
        authenticate(token) { checkOAuth(authorizationCode = authorizationCode, authorizationState = authorizationState) }

    override suspend fun register(
        token: Token,
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        avatar: FileId?,
        cover: FileId?,
        linkPhoneNumber: Boolean,
        linkEmail: Boolean
    ) = authenticate(token) {
        register(
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            avatar = avatar,
            cover = cover,
            linkPhoneNumber = linkPhoneNumber,
            linkEmail = linkEmail,
        )
    }

    override suspend fun cancelLogin(token: Token) =
        authenticate(token) { cancelLogin() }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context) AuthController.() -> R) =
        authenticate(authenticator, token) { block(this, controller) }
}
