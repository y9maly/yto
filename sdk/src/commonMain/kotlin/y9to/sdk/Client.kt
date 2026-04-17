package y9to.sdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import y9to.api.krpc.MainRpc
import y9to.api.types.RefreshToken
import y9to.api.types.Session
import y9to.api.types.Token
import y9to.sdk.internals.RequestController
import y9to.sdk.internals.RpcController


class Client internal constructor(
    internal val scope: CoroutineScope,
    internal val kvStorage: KVStorage,
    internal val httpClient: Any,
    internal val requestController: RequestController,
    internal val rpcController: RpcController,
) {
    val auth = AuthClient(this)
    val user = UserClient(this)
    val post = PostClient(this)
    val file = FileClient(this)
}


expect suspend fun createSdkClient(
    host: String,
    port: Int,
    path: String,
    kvStorage: KVStorage,
): Client
