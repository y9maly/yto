package y9to.sdk.internals

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.rpc.RpcClient
import kotlinx.rpc.internal.utils.InternalRpcApi
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.withService
import y9to.api.krpc.AuthRpc
import y9to.api.types.Token
import kotlin.time.Duration.Companion.milliseconds


@OptIn(InternalRpcApi::class)
class RpcClientController(
    scope: CoroutineScope,
    rpcClientFactory: suspend () -> KrpcClient,
    logger: KLogger,
) {
    private val _rpcClient = MutableStateFlow<RpcClient?>(null)
    val rpcClient = _rpcClient.asStateFlow()

    init {
        scope.launch {
            while (true) {
                try {
                    val rpcClient = try {
                        rpcClientFactory()
                    } catch (t: Throwable) {
                        logger.error(t) { "Failed to create RpcClient" }
                        throw t
                    }

                    logger.trace { "RpcClient created" }
                    _rpcClient.value = rpcClient

                    try {
                        // todo awaitCompletion делает early return если (!isTransportReady)
                        //  Транспорт не инициализируется без вызова, поэтому такой workaround.
                        //  Здесь всегда будет ошибка от сервера, это просто пинг.
                        rpcClient.withService<AuthRpc>().getAuthState(Token(""))
                    } catch (_: Throwable) {}
                    rpcClient.awaitCompletion()

                    _rpcClient.value = null
                } finally {
                    logger.trace { "RpcClient closed. Recreating in 100ms..." }
                    delay(100.milliseconds)
                }
            }
        }
    }
}
