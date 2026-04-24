package y9to.sdk

import kotlinx.coroutines.CoroutineScope
import y9to.sdk.internals.RequestController
import y9to.sdk.internals.RpcController
import y9to.sdk.internals.UpdateCenter


class Client internal constructor(
    internal val scope: CoroutineScope,
    internal val kvStorage: KVStorage,
    internal val httpClient: Any,
    internal val requestController: RequestController,
    internal val rpcController: RpcController,
) {
    internal val updateCenter = UpdateCenter(this)

    val auth = AuthClient(this)
    val user = UserClient(this)
    val post = PostClient(this)
    val file = FileClient(this)
}


expect suspend fun createSdkClient(
    useHttps: Boolean,
    host: String,
    port: Int,
    path: String,
    kvStorage: KVStorage,
): Client
