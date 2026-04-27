package domain.service

import backend.core.types.User
import y9to.libs.stdlib.Union


typealias StartWithPhoneNumberResult = Union<Unit, StartWithPhoneNumberError>
typealias StartWithEmailResult = Union<Unit, StartWithEmailError>
typealias StartWithTelegramOIDCResult = Union<StartWithTelegramOIDCOk, StartWithTelegramOIDCError>

typealias CheckConfirmCodeResult = Union<Unit, CheckConfirmCodeError>
typealias CheckConfirmPasswordResult = Union<Unit, CheckConfirmPasswordError>
typealias CheckTelegramOIDCResult = Union<Unit, CheckTelegramOIDCError>
typealias RegisterDefaultResult = Union<RegisterDefaultOk, RegisterDefaultError>
typealias RegisterViaTelegramResult = Union<RegisterViaTelegramOk, RegisterViaTelegramError>

data class StartWithTelegramOIDCOk(val uri: String)

data class RegisterDefaultOk(val user: User)
data class RegisterViaTelegramOk(val user: User)

sealed interface StartWithPhoneNumberError {
    data object InvalidSessionId : StartWithPhoneNumberError
    data object AlreadyLogInned : StartWithPhoneNumberError
    data object InvalidPhoneNumber : StartWithPhoneNumberError
}

sealed interface StartWithEmailError {
    data object InvalidSessionId : StartWithEmailError
    data object AlreadyLogInned : StartWithEmailError
    data object InvalidEmail : StartWithEmailError
}

sealed interface StartWithTelegramOIDCError {
    data object InvalidSessionId : StartWithTelegramOIDCError
    data object AlreadyLogInned : StartWithTelegramOIDCError
}

sealed interface CheckConfirmCodeError {
    data object Unexpected : CheckConfirmCodeError
    data object LoginAttemptRejected : CheckConfirmCodeError
    data object InvalidSessionId : CheckConfirmCodeError
    data object InvalidConfirmCode : CheckConfirmCodeError
}

sealed interface CheckConfirmPasswordError {
    data object Unexpected : CheckConfirmPasswordError
    data object LoginAttemptRejected : CheckConfirmPasswordError
    data object InvalidSessionId : CheckConfirmPasswordError
    data object InvalidConfirmPassword : CheckConfirmPasswordError
}

sealed interface CheckTelegramOIDCError {
    data object Unexpected : CheckTelegramOIDCError
    data object LoginAttemptRejected : CheckTelegramOIDCError
    data object InvalidSessionId : CheckTelegramOIDCError
    data object InvalidAuthorizationCode : CheckTelegramOIDCError
}

sealed interface RegisterDefaultError {
    data object Unexpected : RegisterDefaultError
    data object LoginAttemptRejected : RegisterDefaultError
    data object InvalidSessionId : RegisterDefaultError
}

sealed interface RegisterViaTelegramError {
    data object Unexpected : RegisterViaTelegramError
    data object LoginAttemptRejected : RegisterViaTelegramError
    data object InvalidSessionId : RegisterViaTelegramError
}
