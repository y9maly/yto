package domain.service

import backend.core.types.AuthState
import backend.core.types.ClientId
import backend.core.types.Session
import backend.core.types.SessionId
import domain.service.result.LogInResult
import domain.service.result.LogoutResult


interface AuthService {
    suspend fun getAuthState(session: SessionId): AuthState?
    suspend fun logIn(session: SessionId, client: ClientId): LogInResult
    suspend fun logOut(session: SessionId): LogoutResult
    suspend fun getAuthenticatedSessions(client: ClientId): Set<SessionId>
    suspend fun existsSession(id: SessionId): Boolean
    suspend fun getSession(id: SessionId): Session?
    suspend fun createSession(): Session
}
