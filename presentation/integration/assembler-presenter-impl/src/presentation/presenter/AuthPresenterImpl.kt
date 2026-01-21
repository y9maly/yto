package presentation.presenter

import domain.service.MainService
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.sessionId
import presentation.mapper.map
import y9to.api.types.AuthState
import y9to.api.types.Session
import backend.core.types.Session as BackendSession
import backend.core.types.AuthState as BackendAuthState


class AuthPresenterImpl(
    private val service: MainService,
) : AuthPresenter {
    context(callContext: CallContext)
    override suspend fun Session(backendSession: BackendSession): Session {
        val isSelf = backendSession.id == callContext.sessionId

        if (!isSelf)
            error("Session ${backendSession.id} cannot be present to session ${callContext.sessionId}")

        return Session(
            id = backendSession.id.map(),
            creationDate = backendSession.creationDate,
        )
    }

    context(callContext: CallContext)
    override suspend fun AuthState(backendAuthState: BackendAuthState): AuthState {
        return when (backendAuthState) {
            is BackendAuthState.Authorized -> AuthState.Authorized(backendAuthState.id.map())
            is BackendAuthState.Unauthorized -> AuthState.Unauthorized
        }
    }
}
