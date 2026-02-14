@file:JvmName("ResultAuthKt")

package y9to.api.types

import kotlinx.serialization.Serializable as S
import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName


typealias LogInResult = Union<LogInOk, LogInError>
typealias LogOutResult = Union<LogOutOk, LogOutError>


typealias LogInOk = Unit
typealias LogOutOk = Unit


@S sealed interface LogInError {
    @S data object UserForSpecifiedAuthMethodNotFound : LogInError
    @S data object AlreadyLogInned : LogInError
}

@S sealed interface LogOutError {
    @S data object AlreadyUnauthorized : LogOutError
}
