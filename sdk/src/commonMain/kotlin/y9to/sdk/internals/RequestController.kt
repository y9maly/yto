package y9to.sdk.internals

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import y9to.api.krpc.RpcCollection
import y9to.api.types.RefreshToken
import y9to.api.types.Token
import y9to.libs.stdlib.coroutines.flow.collectIn
import y9to.libs.stdlib.coroutines.flow.collectLatestIn
import y9to.libs.stdlib.coroutines.flow.firstNotNull
import y9to.sdk.KVStorage
import kotlin.time.Duration.Companion.milliseconds


internal class RequestScope(
    val token: Token,
    val rpc: RpcCollection,
)

internal class RequestController(
    private val scope: CoroutineScope,
    private val kvStorage: KVStorage,
    private val rpcController: RpcController,
    private val logger: KLogger,
) {
    private val _accessToken = MutableStateFlow<Token?>(null)
    val accessToken = _accessToken.asStateFlow()

    init {
        scope.launch {
            _accessToken.value = kvStorage.getString(ACCESS_TOKEN_KEY)?.let(::Token)

            combine(accessToken, rpcController.rpc) { accessToken, rpc ->
                if (accessToken != null)
                    return@combine
                if (rpc == null)
                    return@combine

                val oldRefreshToken = kvStorage.getString(REFRESH_TOKEN_KEY)?.let(::RefreshToken)
                    ?: error("Missing old refresh token")

                val (newRefreshToken, newAccessToken) = rpc.auth.refreshTokens(oldRefreshToken)
                    ?: TODO("Invalid or revoked old refresh token")

                logger.trace { "New access/refresh token pair released" }
                _accessToken.value = newAccessToken
                kvStorage.put(REFRESH_TOKEN_KEY, newRefreshToken.string)
                kvStorage.put(ACCESS_TOKEN_KEY, newAccessToken.string)

                // debounce
                delay(1000.milliseconds)
            }.collectIn(this)
        }
    }

    fun invalidateAccessToken() {
        _accessToken.value = null
        logger.trace { "Access token marked as invalid" }
    }
}

internal suspend fun RequestController.awaitAccessToken(): Token =
    accessToken.firstNotNull()
