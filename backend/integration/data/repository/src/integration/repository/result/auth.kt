package integration.repository.result

import y9to.libs.stdlib.Union


typealias LogInResult = Union<LogInOk, LogInError>
typealias LogOutResult = Union<LogOutOk, LogOutError>


typealias LogInOk = Unit
typealias LogOutOk = Unit


sealed interface LogInError {
    data object AlreadyAuthenticated : LogInError
    data object InvalidSessionId : LogInError
    data object InvalidClientId : LogInError
}

sealed interface LogOutError {
    data object AlreadyLogOuted : LogOutError
    data object InvalidSessionId : LogOutError
}
