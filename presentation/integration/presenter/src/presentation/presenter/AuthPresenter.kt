package presentation.presenter

import backend.core.types.Session as BackendSession
import backend.core.types.AuthState as BackendAuthState
import presentation.integration.callContext.CallContext
import y9to.api.types.AuthState
import y9to.api.types.Session


interface AuthPresenter {
    context(callContext: CallContext)
    suspend fun Session(backendSession: BackendSession): Session

    context(callContext: CallContext)
    suspend fun AuthState(backendAuthState: BackendAuthState): AuthState
}
