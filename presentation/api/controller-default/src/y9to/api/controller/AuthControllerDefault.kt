package y9to.api.controller

import domain.service.LoginService
import domain.service.ServiceCollection
import domain.service.StartWithEmailError
import domain.service.StartWithPhoneNumberError
import domain.service.StartWithTelegramOIDCError
import presentation.assembler.AssemblerCollection
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.presenter.PresenterCollection
import presentation.presenter.map
import y9to.api.types.*
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.successOrElse
import domain.service.result.LogOutError as DomainLogOutError


class AuthControllerDefault(
    val loginService: LoginService,
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
    override suspend fun getLoginState(): LoginState = context {
        val loginState = loginService.getLoginState(sessionId)
        return loginState.map()
    }

    context(_: Context)
    override suspend fun logIn(method: InputAuthMethod): LogInResult = context {
        when (method) {
            is InputAuthMethod.PhoneNumber ->
                loginService.startWithPhoneNumber(sessionId, method.phoneNumber)
                    .successOrElse { error ->
                        return when (error) {
                            StartWithPhoneNumberError.AlreadyLogInned -> LogInError.AlreadyLogInned
                            StartWithPhoneNumberError.InvalidPhoneNumber -> LogInError.UserForSpecifiedAuthMethodNotFound
                            StartWithPhoneNumberError.InvalidSessionId -> error("Invalid token")
                        }.asError()
                    }

            is InputAuthMethod.Email ->
                loginService.startWithEmail(sessionId, method.email)
                    .successOrElse { error ->
                        return when (error) {
                            StartWithEmailError.AlreadyLogInned -> LogInError.AlreadyLogInned
                            StartWithEmailError.InvalidEmail -> LogInError.UserForSpecifiedAuthMethodNotFound
                            StartWithEmailError.InvalidSessionId -> error("Invalid token")
                        }.asError()
                    }

            is InputAuthMethod.Telegram ->
                loginService.startWithTelegramOIDC(sessionId, method.requestPhoneNumber)
                    .successOrElse { error ->
                        return when (error) {
                            StartWithTelegramOIDCError.AlreadyLogInned -> LogInError.AlreadyLogInned
                            StartWithTelegramOIDCError.InvalidSessionId -> error("Invalid token")
                        }.asError()
                    }
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
