package presentation.presenter

import backend.core.types.Session as BackendSession
import backend.core.types.AuthState as BackendAuthState
import presentation.integration.context.Context
import y9to.api.types.AuthState
import y9to.api.types.Session


interface AuthPresenter {
    context(context: Context)
    suspend fun Session(backendSession: BackendSession): Session

    context(context: Context)
    suspend fun AuthState(backendAuthState: BackendAuthState): AuthState
}

context(_: Context, presenter: AuthPresenter)
suspend fun BackendSession.map(): Session = presenter.Session(this)

context(_: Context, presenter: AuthPresenter)
suspend fun BackendAuthState.map(): AuthState = presenter.AuthState(this)
