package integration.repository.result

import y9to.libs.stdlib.Union


typealias LogInResult = Union<LogInOk, LogInError>
typealias LogOutResult = Union<LogOutOk, LogOutError>


typealias LogInOk = Unit
typealias LogOutOk = Unit


sealed interface LogInError {
    data object AlreadyLogInned : LogInError
    data object UnknownSessionId : LogInError
    data object UnknownClientId : LogInError
}

sealed interface LogOutError {
    data object AlreadyLogOuted : LogOutError
    data object UnknownSessionId : LogOutError
}
