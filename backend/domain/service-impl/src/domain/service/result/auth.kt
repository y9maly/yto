package domain.service.result

import domain.service.result.internals.mapError
import integration.repository.result.LogInError as DbLogInError
import integration.repository.result.LogInResult as DbLogInResult
import integration.repository.result.LogOutError as DbLogOutError
import integration.repository.result.LogOutResult as DbLogOutResult


@JvmName("mapLogInResult")
internal fun DbLogInResult.map() = mapError { map() }
internal fun DbLogInError.map(): LogInError = when (this) {
    DbLogInError.AlreadyAuthenticated -> LogInError.AlreadyAuthenticated
    DbLogInError.InvalidClientId -> LogInError.InvalidClientId
    DbLogInError.InvalidSessionId -> LogInError.InvalidSessionId
}

@JvmName("mapLogOutResult")
internal fun DbLogOutResult.map() = mapError { map() }
internal fun DbLogOutError.map(): LogOutError = when (this) {
    DbLogOutError.AlreadyLogOuted -> LogOutError.AlreadyLogOuted
    DbLogOutError.InvalidSessionId -> LogOutError.InvalidSessionId
}
