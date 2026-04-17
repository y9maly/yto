package y9to.api.controller

import domain.service.ServiceCollection
import presentation.assembler.AssemblerCollection
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.presenter.PresenterCollection
import presentation.presenter.map
import y9to.api.types.*
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.successOrElse
import domain.service.result.LogInError as DomainLogInError
import domain.service.result.LogOutError as DomainLogOutError


class AuthControllerDefault(
    private val service: ServiceCollection,
    override val assembler: AssemblerCollection,
    override val presenter: PresenterCollection,
) : AuthController, ControllerDefault {
    override suspend fun createSession(): backend.core.types.SessionId = context {
        val session = service.auth.createSession()
        return session.id
    }

    context(_: Context)
    override suspend fun getSession(): Session = context {
        val session = service.auth.getSession(sessionId)
            ?: error("Invalid token")
        return session.map()
    }

    context(_: Context)
    override suspend fun getAuthState(): AuthState = context {
        val authState = service.auth.getAuthState(sessionId)
            ?: error("Invalid token")
        return authState.map()
    }

    context(_: Context)
    override suspend fun logIn(method: InputAuthMethod): LogInResult = context {
        val user = when (method) {
            is InputAuthMethod.PhoneNumber -> service.user.findByPhoneNumber(method.phoneNumber)
            is InputAuthMethod.Email -> service.user.findByEmail(method.email)
        } ?: return LogInError.UserForSpecifiedAuthMethodNotFound.asError()

        service.auth.logIn(sessionId, user.id)
            .successOrElse { error ->
                return when (error) {
                    DomainLogInError.AlreadyLogInned -> LogInError.AlreadyLogInned
                    DomainLogInError.InvalidSessionId -> error("Invalid token")
                    DomainLogInError.InvalidClientId -> LogInError.UserForSpecifiedAuthMethodNotFound
                }.asError()
            }

        return LogInOk.asOk()
    }

    context(_: Context)
    override suspend fun logOut(): LogOutResult = context {
        service.auth.logOut(sessionId)
            .successOrElse { error ->
                return when (error) {
                    DomainLogOutError.AlreadyLogOuted -> LogOutError.AlreadyUnauthorized
                    DomainLogOutError.InvalidSessionId -> error("Invalid token")
                }.asError()
            }
        return LogOutOk.asOk()
    }
}
