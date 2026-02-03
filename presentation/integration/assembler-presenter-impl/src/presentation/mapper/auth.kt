package presentation.mapper

import y9to.api.types.ClientId
import y9to.api.types.SessionId
import y9to.api.types.UserId
import backend.core.types.ClientId as BackendClientId
import backend.core.types.SessionId as BackendSessionId
import backend.core.types.UserId as BackendUserId


fun BackendSessionId.map() = SessionId(long)
fun SessionId.map() = BackendSessionId(long)

fun BackendClientId.map(): ClientId = when (this) {
    is BackendUserId -> map()
}
fun ClientId.map(): BackendClientId = when (this) {
    is UserId -> map()
}
