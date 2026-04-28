package domain.service

import backend.core.types.AuthState
import backend.core.types.ClientId
import backend.core.types.Session
import backend.core.types.SessionId
import domain.event.AuthStateChanged
import domain.event.SessionCreated
import domain.service.result.LogInResult
import domain.service.result.LogoutResult
import domain.service.result.map
import integration.eventCollector.EventCollector
import integration.repository.RepositoryCollection
import integration.repository.result.LogInError
import kotlin.time.Clock


class AuthServiceImpl(
    private val repo: RepositoryCollection,
    private val eventCollector: EventCollector,
    private val clock: Clock,
) : AuthService {
    override suspend fun getAuthState(session: SessionId): AuthState? {
        return repo.auth.getAuthState(session)
    }

    override suspend fun logIn(session: SessionId, client: ClientId): LogInResult {
        val result = repo.auth.logIn(session, client)

        result
            .onSuccess {
                eventCollector.emit(AuthStateChanged(session, AuthState.Authorized(client)))
            }
            .onError { error ->
                when (error) {
                    LogInError.AlreadyAuthenticated,
                    LogInError.InvalidClientId,
                    LogInError.InvalidSessionId -> {
                        // do nothing
                    }
                }
            }

        return result.map()
    }

    override suspend fun logOut(session: SessionId): LogoutResult {
        val result = repo.auth.logOut(session)

        result
            .onSuccess {
                eventCollector.emit(AuthStateChanged(session, AuthState.Unauthorized))
            }
            .onError { error ->
                when (error) {
                    integration.repository.result.LogOutError.AlreadyLogOuted,
                    integration.repository.result.LogOutError.InvalidSessionId -> {
                        // do nothing
                    }
                }
            }

        return result.map()
    }

    override suspend fun getAuthenticatedSessions(client: ClientId): Set<SessionId> {
        return repo.auth.getAuthenticatedSessions(client)
    }

    override suspend fun existsSession(id: SessionId): Boolean {
        return repo.auth.existsSession(id)
    }

    override suspend fun getSession(id: SessionId): Session? {
        return repo.auth.getSession(id)
    }

    override suspend fun createSession(): Session {
        return repo.auth.createSession(clock.now())
            .also {
                eventCollector.emit(SessionCreated(it))
            }
    }
}
