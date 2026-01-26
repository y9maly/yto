package y9to.sdk

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import y9to.api.types.AuthState
import y9to.api.types.InputAuthMethod
import y9to.api.types.LogInResult
import y9to.api.types.LogOutResult
import y9to.api.types.Session
import y9to.api.types.Token
import kotlin.coroutines.cancellation.CancellationException


class AuthClient internal constructor(
    private val client: Client,
    initialSession: Session,
    internal val token: Token,
) {
    val session = MutableStateFlow(initialSession)

    val authState = flow {
        while (true) {
            try {
                emit(client.rpc.auth.getAuthState(token))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            }

            delay((1500L..2000L).random())
        }
    }
        .distinctUntilChanged()
        .shareIn(client.scope, SharingStarted.WhileSubscribed(5000), 1)

    internal val needResetLocalCache = client.scope.async {
        client.rpc.auth.needResetLocalCache(token)
    }

    suspend fun logIn(method: InputAuthMethod): LogInResult {
        return client.rpc.auth.logIn(token, method)
    }

    suspend fun logOut(): LogOutResult {
        return client.rpc.auth.logOut(token)
    }
}
