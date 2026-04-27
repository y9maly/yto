package presentation.presenter

import domain.service.ServiceCollection
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.mapper.map
import y9to.api.types.AuthState
import y9to.api.types.LoginState
import y9to.api.types.Session
import y9to.libs.stdlib.optional.none
import backend.core.types.Session as BackendSession
import backend.core.types.AuthState as BackendAuthState


class AuthPresenterImpl(
    private val service: ServiceCollection,
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

    context(context: Context)
    override suspend fun LoginState(
        backendLoginState: backend.core.types.LoginState
    ): LoginState = when (backendLoginState) {
        backend.core.types.LoginState.None -> LoginState.None
        is backend.core.types.LoginState.TelegramOIDC -> LoginState.TelegramOIDC(backendLoginState.authorizationUri)
        is backend.core.types.LoginState.WaitConfirmCode -> LoginState.WaitConfirmCode(backendLoginState.digitOnly, backendLoginState.length)
        is backend.core.types.LoginState.WaitPassword -> LoginState.WaitPassword(backendLoginState.hint)
        backend.core.types.LoginState.WaitRegistration.WaitRegistrationDefault -> LoginState.WaitRegistration.WaitRegistrationDefault
        is backend.core.types.LoginState.WaitRegistration.WaitRegistrationViaTelegram -> LoginState.WaitRegistration.WaitRegistrationViaTelegram(
            telegramFirstName = backendLoginState.telegramFirstName,
            telegramLastName = backendLoginState.telegramLastName,
            telegramAvatar = none(), // todo
            telegramPhoneNumber = backendLoginState.telegramPhoneNumber,
            canUseTelegramPhoneNumber = backendLoginState.canUseTelegramPhoneNumber,
        )
    }
}
