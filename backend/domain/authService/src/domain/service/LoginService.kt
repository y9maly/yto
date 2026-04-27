package domain.service

import backend.core.types.FileId
import backend.core.types.LoginState
import backend.core.types.SessionId
import y9to.common.types.Birthday


interface LoginService {
    suspend fun getLoginState(session: SessionId): LoginState

    suspend fun startWithPhoneNumber(session: SessionId, phoneNumber: String): StartWithPhoneNumberResult
    suspend fun startWithEmail(session: SessionId, email: String): StartWithEmailResult
    suspend fun startWithTelegramOIDC(session: SessionId, requestPhoneNumber: Boolean): StartWithTelegramOIDCResult

    suspend fun checkConfirmCode(session: SessionId, code: String): CheckConfirmCodeResult
    suspend fun checkConfirmPassword(session: SessionId, password: String): CheckConfirmPasswordResult
    suspend fun checkTelegramOIDC(session: SessionId, authorizationCode: String, state: String): CheckTelegramOIDCResult

    suspend fun registerDefault(
        session: SessionId,
        firstName: String,
        lastName: String,
        bio: String?,
        birthday: Birthday?,
        cover: FileId?,
        avatar: FileId?,
    ): RegisterDefaultResult

    suspend fun registerViaTelegram(
        session: SessionId,
        useTelegramPhoneNumber: Boolean,
        firstName: String,
        lastName: String,
        bio: String?,
        birthday: Birthday?,
        cover: FileId?,
        avatar: FileId?,
    ): RegisterViaTelegramResult
}
