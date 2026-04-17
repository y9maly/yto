package y9to.sdk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import y9to.api.types.InputAuthMethod
import y9to.api.types.LogInResult
import y9to.api.types.LogOutResult
import y9to.api.types.RefreshToken
import y9to.api.types.Session
import y9to.api.types.Token
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

    val authState = flow {
        while (true) {
            val value = request {
                rpc.auth.getAuthState(token)
            }

            emit(value)

            delay((1500L..2000L).random().milliseconds)
        }
    }
        .distinctUntilChanged()
        .shareIn(client.scope, SharingStarted.WhileSubscribed(5000), 1)

    suspend fun logIn(method: InputAuthMethod): LogInResult {
        return request {
            rpc.auth.logIn(token, method)
                .also { client.requestController.invalidateAccessToken() }
        }
    }

    suspend fun logOut(): LogOutResult {
        return request {
            rpc.auth.logOut(token)
                .also { client.requestController.invalidateAccessToken() }
        }
    }
}
