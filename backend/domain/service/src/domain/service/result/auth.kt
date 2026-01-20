package domain.service.result

import domain.service.result.internals.mapError
import integration.repository.result.LogInResult as DbLogInResult
import integration.repository.result.LogOutResult as DbLogOutResult
import integration.repository.result.LogInError as DbLogInError
import integration.repository.result.LogOutError as DbLogOutError
import y9to.libs.stdlib.Union


typealias LogInResult = Union<LogInOk, LogInError>
typealias LogOutResult = Union<LogOutOk, LogOutError>


typealias LogInOk = Unit
typealias LogOutOk = Unit


sealed interface LogInError {
    data object AlreadyLogInned : LogInError
    data object UnknownSessionId : LogInError
    data object UnknownAuthorizableId : LogInError
}

sealed interface LogOutError {
    data object AlreadyLogOuted : LogOutError
    data object UnknownSessionId : LogOutError
}


@JvmName("mapLogInResult")
fun DbLogInResult.map() = mapError { map() }
fun DbLogInError.map() = when (this) {
    DbLogInError.AlreadyLogInned -> LogInError.AlreadyLogInned
    DbLogInError.UnknownAuthorizableId -> LogInError.UnknownAuthorizableId
    DbLogInError.UnknownSessionId -> LogInError.UnknownSessionId
}

@JvmName("mapLogOutResult")
fun DbLogOutResult.map() = mapError { map() }
fun DbLogOutError.map() = when (this) {
    DbLogOutError.AlreadyLogOuted -> LogOutError.AlreadyLogOuted
    DbLogOutError.UnknownSessionId -> LogOutError.UnknownSessionId
}
