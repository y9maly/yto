package presentation.workers.updatePublisher

import backend.core.types.ClientId
import backend.core.types.SessionId


interface SessionProvider {
    suspend fun client(client: ClientId): Set<SessionId>
}
