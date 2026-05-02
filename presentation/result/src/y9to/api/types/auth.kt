@file:JvmName("ResultAuthKt")

package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName


typealias LogOutResult = Union<Unit, LogOutError>

typealias StartLoginWithPhoneNumberResult = Union<Unit, StartLoginWithPhoneNumberError>
typealias StartLoginWithEmailResult = Union<Unit, StartLoginWithEmailError>
typealias StartLoginWithTelegramOAuthResult = Union<Unit, StartLoginWithTelegramOAuthError>

typealias CheckConfirmCodeResult = Union<Unit, CheckConfirmCodeError>
typealias CheckPassword2FAResult = Union<Unit, CheckPassword2FAError>
typealias CheckOAuthResult = Union<Unit, CheckOAuthError>
typealias RegisterResult = Union<Unit, RegisterError>

// ------ ------ ------

@S sealed interface StartLoginError :
    StartLoginWithPhoneNumberError,
    StartLoginWithEmailError,
    StartLoginWithOAuthError
{
    @SerialName("AlreadyAuthenticated")
    @S data object AlreadyAuthenticated : StartLoginError

    @SerialName("UnavailableLoginMethod")
    @S data object UnavailableLoginMethod : StartLoginError
}

@S sealed interface StartLoginWithPhoneNumberError {
    @SerialName("InvalidPhoneNumber")
    @S data object InvalidPhoneNumber : StartLoginWithPhoneNumberError
}

@S sealed interface StartLoginWithEmailError {
    @SerialName("InvalidEmail")
    @S data object InvalidEmail : StartLoginWithEmailError
}

@SerialName("StartLoginWithOAuthError")
@S sealed interface StartLoginWithOAuthError :
    StartLoginWithTelegramOAuthError
{
    /**
     * Problems with OAuth provider. For example if OAuth provider server returns 500 errors.
     */
    @SerialName("OAuthProviderError")
    @S data object OAuthProviderError : StartLoginWithOAuthError
}

@S sealed interface StartLoginWithTelegramOAuthError {
    @SerialName("PhoneNumberLinkageRequired")
    @S data object PhoneNumberLinkageRequired : StartLoginWithTelegramOAuthError
}

// ------ ------ ------

@SerialName("ContinueLoginError")
@S sealed interface ContinueLoginError :
    CheckConfirmCodeError,
    CheckPassword2FAError,
    CheckOAuthError,
    RegisterError
{
    @SerialName("Unexpected")
    @S data object Unexpected : ContinueLoginError

    @SerialName("LoginAttemptRejected")
    @S data object LoginAttemptRejected : ContinueLoginError
}

@S sealed interface CheckConfirmCodeError {
    @SerialName("InvalidConfirmCode")
    @S data object InvalidConfirmCode : CheckConfirmCodeError
}

@S sealed interface CheckPassword2FAError {
    @SerialName("InvalidPassword2FA")
    @S data object InvalidPassword2FA : CheckPassword2FAError
}

@S sealed interface CheckOAuthError {
    @SerialName("InvalidAuthorizationCode")
    @S data object InvalidAuthorizationCode : CheckOAuthError

    @SerialName("InvalidAuthorizationState")
    @S data object InvalidAuthorizationState : CheckOAuthError
}

@S sealed interface RegisterError {
    @SerialName("CannotLinkPhoneNumber")
    @S data object CannotLinkPhoneNumber : RegisterError

    @SerialName("CannotLinkEmail")
    @S data object CannotLinkEmail : RegisterError

    @SerialName("PhoneNumberLinkageRequired")
    @S data object PhoneNumberLinkageRequired : RegisterError

    @SerialName("EmailLinkageRequired")
    @S data object EmailLinkageRequired : RegisterError
}

// ------ ------ ------

@S sealed interface LogOutError {
    @SerialName("AlreadyUnauthenticated")
    @S data object AlreadyUnauthenticated : LogOutError
}
