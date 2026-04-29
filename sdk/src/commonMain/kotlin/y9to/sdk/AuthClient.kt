package y9to.sdk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.sdk.internals.ClientOwner
import y9to.sdk.internals.request
import kotlin.time.Duration.Companion.milliseconds


class AuthClient internal constructor(
    override val client: Client,
) : ClientOwner {
    val session = MutableStateFlow<Session?>(null)

    init {
        client.scope.launch {
            while (true) {
                session.value = request { rpc.auth.getSession(token) }
                delay((20000L..30000L).random().milliseconds)
            }
        }
    }

    val authState: Flow<AuthState> = channelFlow {
        send(request { rpc.auth.getAuthState(token) })

        client.updateCenter.updates.filterIsInstance<Update.AuthStateChanged>().collect {
            send(it.authState)
        }
    }
        .distinctUntilChanged()
        .shareIn(client.scope, SharingStarted.WhileSubscribed(5000), 1)

    val loginState: Flow<LoginState?> = channelFlow {
        send(request { rpc.auth.getLoginState(token) })

        client.updateCenter.updates.filterIsInstance<Update.LoginStateChanged>().collect {
            send(it.loginState)
        }
    }
        .distinctUntilChanged()
        .shareIn(client.scope, SharingStarted.WhileSubscribed(5000), 1)

    suspend fun logOut(): LogOutResult {
        return request {
            rpc.auth.logOut(token)
        }
    }

    suspend fun startLoginWithPhoneNumber(phoneNumber: String): StartLoginWithPhoneNumberResult {
        return request { rpc.auth.startLoginWithPhoneNumber(token, phoneNumber) }
    }

    suspend fun startLoginWithEmail(email: String): StartLoginWithEmailResult {
        return request { rpc.auth.startLoginWithEmail(token, email) }
    }

    suspend fun startLoginWithTelegramOAuth(requestPhoneNumber: Boolean): StartLoginWithTelegramOAuthResult {
        return request { rpc.auth.startLoginWithTelegramOAuth(token, requestPhoneNumber) }
    }

    suspend fun checkConfirmCode(code: String): CheckConfirmCodeResult {
        return request { rpc.auth.checkConfirmCode(token, code) }
    }

    suspend fun checkPassword2FA(password: String): CheckPassword2FAResult {
        return request { rpc.auth.checkPassword2FA(token, password) }
    }

    suspend fun checkOAuth(authorizationCode: String, authorizationState: String): CheckOAuthResult {
        return request { rpc.auth.checkOAuth(
            token = token,
            authorizationCode = authorizationCode,
            authorizationState = authorizationState
        ) }
    }

    /**
     * @param linkPhoneNumber must be false if [loginState].value.linkPhoneNumberInfo is None
     * @param linkPhoneNumber must be true if [loginState].value.linkPhoneNumberInfo is Mandatory
     * @param linkEmail must be false if [loginState].value.linkEmailInfo is None
     * @param linkEmail must be true if [loginState].value.linkEmailInfo is Mandatory
     */
    suspend fun register(
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        avatar: FileId?,
        cover: FileId?,
        linkPhoneNumber: Boolean,
        linkEmail: Boolean,
    ): RegisterResult {
        return request { rpc.auth.register(
            token = token,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            avatar = avatar,
            cover = cover,
            linkPhoneNumber = linkPhoneNumber,
            linkEmail = linkEmail,
        ) }
    }

    suspend fun cancelLogin() {
        request { rpc.auth.cancelLogin(token) }
    }
}
