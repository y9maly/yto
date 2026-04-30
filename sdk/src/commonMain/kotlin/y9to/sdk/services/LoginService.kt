package y9to.sdk.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import y9to.api.types.CheckConfirmCodeResult
import y9to.api.types.CheckOAuthResult
import y9to.api.types.CheckPassword2FAResult
import y9to.api.types.FileId
import y9to.api.types.LogOutResult
import y9to.api.types.LoginState
import y9to.api.types.RegisterResult
import y9to.api.types.StartLoginWithEmailResult
import y9to.api.types.StartLoginWithPhoneNumberResult
import y9to.api.types.StartLoginWithTelegramOAuthResult
import y9to.common.types.Birthday
import y9to.sdk.Client


interface LoginService {
    suspend fun startLoginWithPhoneNumber(phoneNumber: String): StartLoginWithPhoneNumberResult
    suspend fun startLoginWithEmail(email: String): StartLoginWithEmailResult
    suspend fun startLoginWithTelegramOAuth(requestPhoneNumber: Boolean): StartLoginWithTelegramOAuthResult

    suspend fun checkConfirmCode(code: String): CheckConfirmCodeResult
    suspend fun checkPassword2FA(password: String): CheckPassword2FAResult
    suspend fun checkOAuth(authorizationCode: String, authorizationState: String): CheckOAuthResult

    /**
     * @param linkPhoneNumber must be false if loginState.value.linkPhoneNumberInfo is None
     * @param linkPhoneNumber must be true if loginState.value.linkPhoneNumberInfo is Mandatory
     * @param linkEmail must be false if loginState.value.linkEmailInfo is None
     * @param linkEmail must be true if loginState.value.linkEmailInfo is Mandatory
     */
    suspend fun register(
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        avatar: FileId?,
        cover: FileId?,
        linkPhoneNumber: Boolean,
        linkEmail: Boolean,
    ): RegisterResult

    suspend fun cancelLogin()
}


class LoginServiceDefault(private val client: Client) : LoginService {
    override suspend fun startLoginWithPhoneNumber(phoneNumber: String): StartLoginWithPhoneNumberResult {
        return client.auth.startLoginWithPhoneNumber(phoneNumber)
    }

    override suspend fun startLoginWithEmail(email: String): StartLoginWithEmailResult {
        return client.auth.startLoginWithEmail(email)
    }

    override suspend fun startLoginWithTelegramOAuth(requestPhoneNumber: Boolean): StartLoginWithTelegramOAuthResult {
        return client.auth.startLoginWithTelegramOAuth(requestPhoneNumber)
    }

    override suspend fun checkConfirmCode(code: String): CheckConfirmCodeResult {
        return client.auth.checkConfirmCode(code)
    }

    override suspend fun checkPassword2FA(password: String): CheckPassword2FAResult {
        return client.auth.checkPassword2FA(password)
    }

    override suspend fun checkOAuth(
        authorizationCode: String,
        authorizationState: String
    ): CheckOAuthResult {
        return client.auth.checkOAuth(
            authorizationCode = authorizationCode,
            authorizationState = authorizationState,
        )
    }

    override suspend fun register(
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        avatar: FileId?,
        cover: FileId?,
        linkPhoneNumber: Boolean,
        linkEmail: Boolean
    ): RegisterResult {
        return client.auth.register(
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

    override suspend fun cancelLogin() {
        client.auth.cancelLogin()
    }
}
