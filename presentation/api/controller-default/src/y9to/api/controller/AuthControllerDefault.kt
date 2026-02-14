package y9to.api.controller

import domain.service.MainService
import presentation.assembler.MainAssembler
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.presenter.MainPresenter
import presentation.presenter.map
import y9to.api.types.AuthState
import y9to.api.types.InputAuthMethod
import y9to.api.types.LogInError
import y9to.api.types.LogInOk
import y9to.api.types.LogInResult
import y9to.api.types.LogOutError
import y9to.api.types.LogOutOk
import y9to.api.types.LogOutResult
import y9to.api.types.Session
import y9to.api.types.SessionId
import y9to.api.types.Token
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.successOrElse
import domain.service.result.LogInError as DomainLogInError
import domain.service.result.LogOutError as DomainLogOutError


class AuthControllerDefault(
    private val service: MainService,
    override val assembler: MainAssembler,
    override val presenter: MainPresenter,
) : AuthController, ControllerDefault {
    override suspend fun createSession(): Token = context {
        val session = service.auth.createSession()
        return Token(Token.Unsafe(
            session = SessionId(session.id.long),
            apiVersion = "0.0.1"
        ))
    }

    private val reset = hashSetOf<backend.core.types.SessionId>()
    private val resetLock = Any()

    context(_: Context)
    override suspend fun needResetLocalCache(): Boolean = context {
        synchronized(resetLock) {
            if (sessionId in reset)
                return false
            reset.add(sessionId)
            return true
        }
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
                    DomainLogInError.UnknownSessionId -> error("Invalid token")
                    DomainLogInError.UnknownClientId -> LogInError.UserForSpecifiedAuthMethodNotFound
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
                    DomainLogOutError.UnknownSessionId -> error("Invalid token")
                }.asError()
            }
        return LogOutOk.asOk()
    }
}
