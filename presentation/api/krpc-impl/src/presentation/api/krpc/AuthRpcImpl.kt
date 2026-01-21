package presentation.api.krpc

import domain.service.MainService
import presentation.api.krpc.internals.authenticate
import presentation.authenticator.Authenticator
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.sessionId
import presentation.presenter.MainPresenter
import y9to.api.krpc.AuthRpc
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
import java.util.concurrent.ConcurrentSkipListSet
import domain.service.result.LogInError as DomainLogInError
import domain.service.result.LogOutError as DomainLogOutError


class AuthRpcImpl(
    private val authenticator: Authenticator,
    private val service: MainService,
    private val presenter: MainPresenter,
) : AuthRpc {
    override suspend fun createSession(): Token {
        val session = service.auth.createSession()
        return Token(Token.Unsafe(
            session = SessionId(session.id.long),
            apiVersion = "0.0.1"
        ))
    }

    private val reset = hashSetOf<backend.core.types.SessionId>()
    private val resetLock = Any()
    override suspend fun needResetLocalCache(token: Token): Boolean = authenticate(token) {
        synchronized(resetLock) {
            if (sessionId in reset)
                return false
            reset.add(sessionId)
            return true
        }
    }

    override suspend fun getSession(token: Token): Session = authenticate(token) {
        val session = service.auth.getSession(sessionId)
            ?: error("Invalid token")
        val remoteSession = presenter.auth.Session(session)
        return remoteSession
    }

    override suspend fun getAuthState(token: Token): AuthState = authenticate(token) {
        val authState = service.auth.getAuthState(sessionId)
            ?: error("Invalid token")
        val remoteAuthState = presenter.auth.AuthState(authState)
        return remoteAuthState
    }

    override suspend fun logIn(
        token: Token,
        method: InputAuthMethod
    ): LogInResult = authenticate(token) {
        val user = when (method) {
            is InputAuthMethod.PhoneNumber -> service.user.findByPhoneNumber(method.phoneNumber)
            is InputAuthMethod.Email -> service.user.findByEmail(method.email)
        } ?: return LogInError.UserForSpecifiedAuthMethodNotFound.asError()
        service.auth.logIn(sessionId, user.id)
            .successOrElse { error ->
                return when (error) {
                    DomainLogInError.AlreadyLogInned -> LogInError.AlreadyLogInned
                    DomainLogInError.UnknownSessionId -> error("Invalid token")
                    DomainLogInError.UnknownAuthorizableId -> LogInError.UserForSpecifiedAuthMethodNotFound
                }.asError()
            }
        return LogInOk.asOk()
    }

    override suspend fun logOut(token: Token): LogOutResult = authenticate(token) {
        service.auth.logOut(sessionId)
            .successOrElse { error ->
                return when (error) {
                    DomainLogOutError.AlreadyLogOuted -> LogOutError.AlreadyUnauthorized
                    DomainLogOutError.UnknownSessionId -> error("Invalid token")
                }.asError()
            }
        return LogOutOk.asOk()
    }

    private suspend inline fun <R> authenticate(token: Token, block: CallContext.() -> R) =
        authenticate(authenticator, token, block)
}

