package presentation.mapper

import y9to.api.types.AuthorizableId
import y9to.api.types.SessionId
import y9to.api.types.UserId
import backend.core.types.AuthorizableId as BackendAuthorizableId
import backend.core.types.SessionId as BackendSessionId
import backend.core.types.UserId as BackendUserId


fun BackendSessionId.map() = SessionId(long)
fun SessionId.map() = BackendSessionId(long)

fun BackendAuthorizableId.map(): AuthorizableId = when (this) {
    is BackendUserId -> map()
}
fun AuthorizableId.map(): BackendAuthorizableId = when (this) {
    is UserId -> map()
}
