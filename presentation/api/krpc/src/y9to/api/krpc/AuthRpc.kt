package y9to.api.krpc

import kotlinx.rpc.annotations.Rpc
import y9to.api.types.*
import y9to.common.types.Birthday


@Rpc
interface AuthRpc {
    suspend fun createSession(): Pair<RefreshToken, Token>

    /**
     * @return null if refresh token expired/revoked/invalid
     */
    suspend fun refreshTokens(refreshToken: RefreshToken): Pair<RefreshToken, Token>?

    suspend fun getSession(token: Token): Session
    suspend fun getAuthState(token: Token): AuthState
    suspend fun logOut(token: Token): LogOutResult

    suspend fun getLoginState(token: Token): LoginState?
    suspend fun startLoginWithPhoneNumber(token: Token, phoneNumber: String): StartLoginWithPhoneNumberResult
    suspend fun startLoginWithEmail(token: Token, email: String): StartLoginWithEmailResult
    suspend fun startLoginWithTelegramOAuth(token: Token, requestPhoneNumber: Boolean): StartLoginWithTelegramOAuthResult

    suspend fun checkConfirmCode(token: Token, code: String): CheckConfirmCodeResult
    suspend fun checkPassword2FA(token: Token, password: String): CheckPassword2FAResult
    suspend fun checkOAuth(token: Token, authorizationCode: String, authorizationState: String): CheckOAuthResult

    /**
     * @param linkPhoneNumber must be false if [getLoginState].linkPhoneNumberInfo is None
     * @param linkPhoneNumber must be true if [getLoginState].linkPhoneNumberInfo is Mandatory
     * @param linkEmail must be false if [getLoginState].linkEmailInfo is None
     * @param linkEmail must be true if [getLoginState].linkEmailInfo is Mandatory
     */
    suspend fun register(
        token: Token,
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        avatar: FileId?,
        cover: FileId?,
        linkPhoneNumber: Boolean,
        linkEmail: Boolean,
    ): RegisterResult
}
