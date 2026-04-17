package presentation.infra.jwtManager

import backend.core.types.AuthState
import backend.core.types.SessionId


data class AccessTokenPayload(
    val sessionId: SessionId,
    val authState: AuthState,
)
