package presentation.workers.updatePublisher

import backend.core.types.ClientId
import backend.core.types.SessionId
import domain.service.AuthService


class SessionProviderService(
    private val authService: AuthService
) : SessionProvider {
    override suspend fun client(client: ClientId): Set<SessionId> {
        return authService.getAuthenticatedSessions(client)
    }
}
