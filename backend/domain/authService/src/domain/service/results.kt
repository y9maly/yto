package domain.service

import y9to.libs.stdlib.Union


typealias StartWithPhoneNumberResult = Union<Unit, StartWithPhoneNumberError>
typealias StartWithEmailResult = Union<Unit, StartWithEmailError>
typealias StartWithTelegramOAuthResult = Union<Unit, StartWithTelegramOAuthError>

typealias CheckConfirmCodeResult = Union<Unit, CheckConfirmCodeError>
typealias CheckPassword2FAResult = Union<Unit, CheckPassword2FAError>
typealias CheckOAuthResult = Union<Unit, CheckOAuthError>
typealias RegisterResult = Union<Unit, RegisterError>

// ------ ------ ------

sealed interface StartLoginError :
    StartWithPhoneNumberError,
    StartWithEmailError,
    StartWithOAuthError
{
    data object AlreadyAuthenticated : StartLoginError
    data object UnavailableLoginMethod : StartLoginError
    data object InvalidSessionId : StartLoginError
}

sealed interface StartWithPhoneNumberError {
    data object InvalidPhoneNumber : StartWithPhoneNumberError
}

sealed interface StartWithEmailError {
    data object InvalidEmail : StartWithEmailError
}

sealed interface StartWithOAuthError :
    StartWithTelegramOAuthError
{
    /**
     * Problems with OAuth provider. For example if OAuth provider server returns 500 errors.
     */
    data object OAuthProviderError : StartWithOAuthError
}

sealed interface StartWithTelegramOAuthError {
    data object PhoneNumberLinkageRequired : StartWithTelegramOAuthError
}

// ------ ------ ------

sealed interface ContinueLoginError :
    CheckConfirmCodeError,
    CheckPassword2FAError,
    CheckOAuthError,
    RegisterError
{
    data object Unexpected : ContinueLoginError
    data object LoginAttemptRejected : ContinueLoginError
    data object InvalidSessionId : ContinueLoginError
}

sealed interface CheckConfirmCodeError {
    data object InvalidConfirmCode : CheckConfirmCodeError
}

sealed interface CheckPassword2FAError {
    data object InvalidPassword2FA : CheckPassword2FAError
}

sealed interface CheckOAuthError {
    data object InvalidAuthorizationCode : CheckOAuthError
    data object InvalidAuthorizationState : CheckOAuthError
}

sealed interface RegisterError {
    data object CannotLinkPhoneNumber : RegisterError
    data object CannotLinkEmail : RegisterError
    data object PhoneNumberLinkageRequired : RegisterError
    data object EmailLinkageRequired : RegisterError
}
