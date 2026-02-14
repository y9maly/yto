package presentation.presenter

import domain.service.MainService
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.mapper.map
import y9to.api.types.AuthState
import y9to.api.types.Session
import backend.core.types.Session as BackendSession
import backend.core.types.AuthState as BackendAuthState


class AuthPresenterImpl(
    private val service: MainService,
) : AuthPresenter {
    context(context: Context)
    override suspend fun Session(backendSession: BackendSession): Session {
        val isSelf = backendSession.id == sessionId

        if (!isSelf)
            error("Session ${backendSession.id} cannot be present to session $sessionId")

        return Session(
            id = backendSession.id.map(),
            creationDate = backendSession.creationDate,
        )
    }

    context(context: Context)
    override suspend fun AuthState(backendAuthState: BackendAuthState): AuthState {
        return when (backendAuthState) {
            is BackendAuthState.Authorized -> AuthState.Authorized(backendAuthState.id.map())
            is BackendAuthState.Unauthorized -> AuthState.Unauthorized
        }
    }
}
