package presentation.infra.jwtManager

import backend.core.types.SessionId
import domain.service.AuthService


class PayloadProviderDefault(
    private val authService: AuthService
) : PayloadProvider {
    override suspend fun getAccessTokenPayload(forSession: SessionId): AccessTokenPayload? {
        val authState = authService.getAuthState(forSession)
            ?: return null

        return AccessTokenPayload(
            sessionId = forSession,
            authState = authState,
        )
    }
}
