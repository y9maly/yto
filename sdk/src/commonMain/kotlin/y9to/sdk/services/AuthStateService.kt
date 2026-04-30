package y9to.sdk.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import y9to.api.types.AuthState
import y9to.sdk.Client


interface AuthStateService {
    val authState: Flow<AuthState>
    val canLogout: Flow<Boolean>
}

class AuthStateServiceDefault(private val client: Client) : AuthStateService {
    override val authState = client.auth.authState
    override val canLogout = flowOf(true)
}

val AuthStateService.isAuthenticated get() = authState.map { it is AuthState.Authorized }
val AuthStateService.clientId get() = authState.map { it.idOrNull() }
