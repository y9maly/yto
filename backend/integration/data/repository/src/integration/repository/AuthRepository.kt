package integration.repository

import backend.core.types.AuthState
import backend.core.types.ClientId
import backend.core.types.Session
import backend.core.types.SessionId
import integration.repository.result.LogInResult
import integration.repository.result.LogOutResult
import kotlin.time.Instant


interface AuthRepository {
    suspend fun createSession(creationDate: Instant): Session
    suspend fun getSession(id: SessionId): Session?
    suspend fun existsSession(id: SessionId): Boolean
    suspend fun getAuthState(session: SessionId): AuthState?
    suspend fun logIn(session: SessionId, client: ClientId): LogInResult
    suspend fun logOut(session: SessionId): LogOutResult
    suspend fun getAuthenticatedSessions(client: ClientId): Set<SessionId>
}
