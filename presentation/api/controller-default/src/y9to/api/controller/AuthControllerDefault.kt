package y9to.api.controller

import domain.service.CheckConfirmCodeError
import domain.service.CheckOAuthError
import domain.service.CheckPassword2FAError
import domain.service.ContinueLoginError
import domain.service.LoginService
import domain.service.RegisterError
import domain.service.ServiceCollection
import domain.service.StartLoginError
import domain.service.StartWithEmailError
import domain.service.StartWithOAuthError
import domain.service.StartWithPhoneNumberError
import domain.service.StartWithTelegramOAuthError
import presentation.assembler.AssemblerCollection
import presentation.assembler.map
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.presenter.PresenterCollection
import presentation.presenter.map
import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.mapError
import y9to.libs.stdlib.successOrElse
import domain.service.result.LogOutError as DomainLogOutError


class AuthControllerDefault(
    private val loginService: LoginService,
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
    override suspend fun logOut(): LogOutResult = context {
        service.auth.logOut(sessionId)
            .successOrElse { error ->
                return when (error) {
                    DomainLogOutError.AlreadyLogOuted -> LogOutError.AlreadyUnauthenticated
                    DomainLogOutError.InvalidSessionId -> error("Invalid token")
                }.asError()
            }
        return Unit.asOk()
    }

    context(_: Context)
    override suspend fun getLoginState(): LoginState? = context {
        val loginState = loginService.getLoginState(sessionId)
        return loginState?.map()
    }

    context(_: Context)
    override suspend fun startLoginWithPhoneNumber(phoneNumber: String): StartLoginWithPhoneNumberResult = context {
        loginService.startWithPhoneNumber(sessionId, phoneNumber)
            .mapError { error ->
                when (error) {
                    StartWithPhoneNumberError.InvalidPhoneNumber -> StartLoginWithPhoneNumberError.InvalidPhoneNumber
                    StartLoginError.AlreadyAuthenticated -> y9to.api.types.StartLoginError.AlreadyAuthenticated
                    StartLoginError.UnavailableLoginMethod -> y9to.api.types.StartLoginError.UnavailableLoginMethod
                    StartLoginError.InvalidSessionId -> error("Invalid token")
                }
            }
    }

    context(_: Context)
    override suspend fun startLoginWithEmail(email: String): StartLoginWithEmailResult = context {
        loginService.startWithEmail(sessionId, email)
            .mapError { error ->
                when (error) {
                    StartWithEmailError.InvalidEmail -> StartLoginWithEmailError.InvalidEmail
                    StartLoginError.AlreadyAuthenticated -> y9to.api.types.StartLoginError.AlreadyAuthenticated
                    StartLoginError.UnavailableLoginMethod -> y9to.api.types.StartLoginError.UnavailableLoginMethod
                    StartLoginError.InvalidSessionId -> error("Invalid token")
                }
            }
    }

    context(_: Context)
    override suspend fun startLoginWithTelegramOAuth(requestPhoneNumber: Boolean): StartLoginWithTelegramOAuthResult = context {
        loginService.startWithTelegramOAuth(sessionId, requestPhoneNumber)
            .mapError { error ->
                when (error) {
                    StartWithOAuthError.OAuthProviderError -> StartLoginWithOAuthError.OAuthProviderError
                    StartWithTelegramOAuthError.PhoneNumberLinkageRequired -> StartLoginWithTelegramOAuthError.PhoneNumberLinkageRequired
                    StartLoginError.AlreadyAuthenticated -> y9to.api.types.StartLoginError.AlreadyAuthenticated
                    StartLoginError.UnavailableLoginMethod -> y9to.api.types.StartLoginError.UnavailableLoginMethod
                    StartLoginError.InvalidSessionId -> error("Invalid token")
                }
            }
    }

    context(_: Context)
    override suspend fun checkConfirmCode(code: String) = context {
        loginService.checkConfirmCode(sessionId, code)
            .mapError { error ->
                when (error) {
                    CheckConfirmCodeError.InvalidConfirmCode -> y9to.api.types.CheckConfirmCodeError.InvalidConfirmCode
                    ContinueLoginError.LoginAttemptRejected -> y9to.api.types.ContinueLoginError.LoginAttemptRejected
                    ContinueLoginError.Unexpected -> y9to.api.types.ContinueLoginError.Unexpected
                    ContinueLoginError.InvalidSessionId -> error("Invalid token")
                }
            }
    }

    context(_: Context)
    override suspend fun checkPassword2FA(password: String) = context {
        loginService.checkPassword2FA(sessionId, password)
            .mapError { error ->
                when (error) {
                    CheckPassword2FAError.InvalidPassword2FA -> y9to.api.types.CheckPassword2FAError.InvalidPassword2FA
                    ContinueLoginError.LoginAttemptRejected -> y9to.api.types.ContinueLoginError.LoginAttemptRejected
                    ContinueLoginError.Unexpected -> y9to.api.types.ContinueLoginError.Unexpected
                    ContinueLoginError.InvalidSessionId -> error("Invalid token")
                }
            }
    }

    context(_: Context)
    override suspend fun checkOAuth(authorizationCode: String, authorizationState: String): CheckOAuthResult = context {
        loginService.checkOAuth(
            session = sessionId,
            authorizationCode = authorizationCode,
            authorizationState = authorizationState
        ).mapError { error ->
            when (error) {
                CheckOAuthError.InvalidAuthorizationCode -> y9to.api.types.CheckOAuthError.InvalidAuthorizationCode
                CheckOAuthError.InvalidAuthorizationState -> y9to.api.types.CheckOAuthError.InvalidAuthorizationState
                ContinueLoginError.LoginAttemptRejected -> y9to.api.types.ContinueLoginError.LoginAttemptRejected
                ContinueLoginError.Unexpected -> y9to.api.types.ContinueLoginError.Unexpected
                ContinueLoginError.InvalidSessionId -> error("Invalid token")
            }
        }
    }

    context(_: Context)
    override suspend fun register(
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        avatar: FileId?,
        cover: FileId?,
        linkPhoneNumber: Boolean,
        linkEmail: Boolean
    ): RegisterResult = context {
        loginService.register(
            session = sessionId,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            avatar = avatar?.map(),
            cover = cover?.map(),
            linkPhoneNumber = linkPhoneNumber,
            linkEmail = linkEmail,
        ).mapError { error ->
            when (error) {
                RegisterError.CannotLinkEmail -> y9to.api.types.RegisterError.CannotLinkEmail
                RegisterError.CannotLinkPhoneNumber -> y9to.api.types.RegisterError.CannotLinkPhoneNumber
                RegisterError.EmailLinkageRequired -> y9to.api.types.RegisterError.EmailLinkageRequired
                RegisterError.PhoneNumberLinkageRequired -> y9to.api.types.RegisterError.PhoneNumberLinkageRequired
                ContinueLoginError.Unexpected -> y9to.api.types.ContinueLoginError.Unexpected
                ContinueLoginError.LoginAttemptRejected -> y9to.api.types.ContinueLoginError.LoginAttemptRejected
                ContinueLoginError.InvalidSessionId -> error("Invalid token")
            }
        }
    }

    context(_: Context)
    override suspend fun cancelLogin() {
        loginService.cancelLogin(sessionId)
    }
}
