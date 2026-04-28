package domain.service.result

import y9to.libs.stdlib.Union


typealias LogInResult = Union<Unit, LogInError>
typealias LogoutResult = Union<Unit, LogOutError>


sealed interface LogInError {
    data object AlreadyAuthenticated : LogInError
    data object InvalidSessionId : LogInError
    data object InvalidClientId : LogInError
}

sealed interface LogOutError {
    data object AlreadyLogOuted : LogOutError
    data object InvalidSessionId : LogOutError
}
