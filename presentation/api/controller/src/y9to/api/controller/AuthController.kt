package y9to.api.controller

import backend.core.types.SessionId
import presentation.integration.context.Context
import y9to.api.types.AuthState
import y9to.api.types.CheckConfirmCodeResult
import y9to.api.types.CheckOAuthResult
import y9to.api.types.CheckPassword2FAResult
import y9to.api.types.FileId
import y9to.api.types.LogOutResult
import y9to.api.types.LoginState
import y9to.api.types.RegisterResult
import y9to.api.types.Session
import y9to.api.types.StartLoginWithEmailResult
import y9to.api.types.StartLoginWithPhoneNumberResult
import y9to.api.types.StartLoginWithTelegramOAuthResult
import y9to.common.types.Birthday


interface AuthController {
    suspend fun createSession(): SessionId

    context(_: Context) suspend fun getSession(): Session
    context(_: Context) suspend fun getAuthState(): AuthState
    context(_: Context) suspend fun logOut(): LogOutResult

    context(_: Context) suspend fun getLoginState(): LoginState?
    context(_: Context) suspend fun startLoginWithPhoneNumber(phoneNumber: String): StartLoginWithPhoneNumberResult
    context(_: Context) suspend fun startLoginWithEmail(email: String): StartLoginWithEmailResult
    context(_: Context) suspend fun startLoginWithTelegramOAuth(requestPhoneNumber: Boolean): StartLoginWithTelegramOAuthResult

    context(_: Context) suspend fun checkConfirmCode(code: String): CheckConfirmCodeResult
    context(_: Context) suspend fun checkPassword2FA(password: String): CheckPassword2FAResult
    context(_: Context) suspend fun checkOAuth(authorizationCode: String, authorizationState: String): CheckOAuthResult

    /**
     * @param linkPhoneNumber must be false if [getLoginState].linkPhoneNumberInfo is None
     * @param linkPhoneNumber must be true if [getLoginState].linkPhoneNumberInfo is Mandatory
     * @param linkEmail must be false if [getLoginState].linkEmailInfo is None
     * @param linkEmail must be true if [getLoginState].linkEmailInfo is Mandatory
     */
    context(_: Context) suspend fun register(
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
