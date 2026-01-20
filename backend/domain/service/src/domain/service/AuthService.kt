package domain.service

import backend.core.types.AuthState
import backend.core.types.AuthorizableId
import backend.core.types.Session
import backend.core.types.SessionId
import domain.service.result.LogInResult
import domain.service.result.LogOutResult
import domain.service.result.map
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass
import kotlin.time.Clock


class AuthService @InterfaceClass constructor(
    private val repo: MainRepository,
    private val clock: Clock,
) {
    suspend fun getAuthState(session: SessionId): AuthState? {
        return repo.auth.getAuthState(session)
    }

    suspend fun logIn(session: SessionId, authorizable: AuthorizableId): LogInResult {
        return repo.auth.logIn(session, authorizable).map()
    }

    suspend fun logOut(session: SessionId): LogOutResult {
        return repo.auth.logOut(session).map()
    }

    suspend fun existsSession(id: SessionId): Boolean {
        return repo.auth.existsSession(id)
    }

    suspend fun getSession(id: SessionId): Session? {
        return repo.auth.getSession(id)
    }

    suspend fun createSession(): Session {
        return repo.auth.createSession(clock.now())
    }
}
