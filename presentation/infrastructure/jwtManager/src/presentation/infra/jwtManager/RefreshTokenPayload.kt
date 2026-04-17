package presentation.infra.jwtManager

import backend.core.types.SessionId


data class RefreshTokenPayload(
    val sessionId: SessionId,
)
