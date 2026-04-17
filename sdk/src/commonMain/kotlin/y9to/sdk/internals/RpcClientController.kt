package y9to.sdk.internals

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.rpc.RpcClient
import kotlinx.rpc.krpc.client.KrpcClient
import kotlin.time.Duration.Companion.milliseconds


class RpcClientController(
    scope: CoroutineScope,
    rpcClientFactory: suspend () -> KrpcClient,
) {
    private val _rpcClient = MutableStateFlow<RpcClient?>(null)
    val rpcClient = _rpcClient.asStateFlow()

    init {
        scope.launch {
            while (true) {
                try {
                    val debounce = launch { delay(1000.milliseconds) }
                    val rpcClient = rpcClientFactory()
                    _rpcClient.value = rpcClient
                    rpcClient.awaitCompletion()
                    debounce.join()
                    _rpcClient.value = null
                } catch (e: Exception) {
                    e.printStackTrace()
                    _rpcClient.value = null
                    delay(1000.milliseconds)
                }
            }
        }
    }
}
