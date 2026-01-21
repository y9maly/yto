package y9to.api.types

import kotlinx.serialization.Serializable as s
import y9to.libs.stdlib.Union


typealias LogInResult = Union<LogInOk, LogInError>
typealias LogOutResult = Union<LogOutOk, LogOutError>


typealias LogInOk = Unit
typealias LogOutOk = Unit


@s sealed interface LogInError {
    @s data object UserForSpecifiedAuthMethodNotFound : LogInError
    @s data object AlreadyLogInned : LogInError
}

@s sealed interface LogOutError {
    @s data object AlreadyUnauthorized : LogOutError
}
