package domain.service

import backend.core.types.FileId
import backend.core.types.LoginCapabilities
import backend.core.types.LoginState
import backend.core.types.SessionId
import y9to.common.types.Birthday


interface LoginService {
    suspend fun getLoginCapabilities(session: SessionId): LoginCapabilities
    suspend fun getLoginState(session: SessionId): LoginState?

    suspend fun startWithPhoneNumber(session: SessionId, phoneNumber: String): StartWithPhoneNumberResult
    suspend fun startWithEmail(session: SessionId, email: String): StartWithEmailResult

    /**
     * @param requestPhoneNumber must be true if [getLoginCapabilities].requiredToLinkPhoneNumberWhileTelegramOAuthRegistration == true
     */
    suspend fun startWithTelegramOAuth(session: SessionId, requestPhoneNumber: Boolean): StartWithTelegramOAuthResult

    suspend fun checkConfirmCode(session: SessionId, code: String): CheckConfirmCodeResult
    suspend fun checkPassword2FA(session: SessionId, password: String): CheckPassword2FAResult
    suspend fun checkOAuth(session: SessionId, authorizationCode: String, authorizationState: String): CheckOAuthResult

    /**
     * @param linkPhoneNumber must be false if [getLoginState].linkPhoneNumberInfo is None
     * @param linkPhoneNumber must be true if [getLoginState].linkPhoneNumberInfo is Mandatory
     * @param linkEmail must be false if [getLoginState].linkEmailInfo is None
     * @param linkEmail must be true if [getLoginState].linkEmailInfo is Mandatory
     */
    suspend fun register(
        session: SessionId,
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        avatar: FileId?,
        cover: FileId?,
        linkPhoneNumber: Boolean,
        linkEmail: Boolean,
    ): RegisterResult

    suspend fun cancelLogin(session: SessionId)
}
