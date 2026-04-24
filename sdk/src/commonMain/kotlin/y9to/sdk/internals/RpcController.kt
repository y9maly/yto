package y9to.sdk.internals

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.rpc.withService
import y9to.api.krpc.RpcCollection
import y9to.libs.stdlib.coroutines.flow.firstNotNull


class RpcController(
    scope: CoroutineScope,
    rpcClientController: RpcClientController,
) {
    val rpc = rpcClientController.rpcClient.map { rpcClient ->
        if (rpcClient == null)
            return@map null

        RpcCollection(
            rpcClient.withService(),
            rpcClient.withService(),
            rpcClient.withService(),
            rpcClient.withService(),
            rpcClient.withService(),
        )
    }.stateIn(scope, SharingStarted.Eagerly, null)
}

internal suspend fun RpcController.awaitRpc(): RpcCollection =
    rpc.firstNotNull()
