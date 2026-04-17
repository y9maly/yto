package presentation.authenticator

import backend.core.types.AuthState
import backend.core.types.SessionId
import y9to.libs.stdlib.optional.Optional


sealed interface AuthenticateResult {
    data class Ok(
        val sessionId: SessionId,
        val authState: Optional<AuthState>,
    ) : AuthenticateResult

    data object ExpiredToken : AuthenticateResult
    data object RevokedToken : AuthenticateResult
    data object InvalidToken : AuthenticateResult
}
