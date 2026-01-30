package y9to.sdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import y9to.api.krpc.MainRpc
import y9to.api.types.Session
import y9to.api.types.Token


internal class ClientNetwork {
    val connected = MutableStateFlow(false)
}

class Client internal constructor(
    token: Token,
    initialSession: Session,
    internal val scope: CoroutineScope,
    private val rpcFactory: suspend () -> Pair<Job, MainRpc>,
) {
    init {
        requestRpc()
    }

    internal val token get() = auth.token
    val auth = AuthClient(this, initialSession, token)
    val user = UserClient(this)
    val feed = FeedClient(this)
    val post = PostClient(this)

    private var _rpc: Pair<Job, MainRpc>? = null
    internal val rpc: MainRpc get() {
        val value = _rpc
        if (value == null || !value.first.isActive) {
            _rpc = null
            requestRpc()
            error("No RPC: Network error")
        }
        return value.second
    }

    private var rpcUnderConstruction = false
    private fun requestRpc() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            if (rpcUnderConstruction) {
                return@launch
            }
            rpcUnderConstruction = true
            try {
                _rpc = rpcFactory()
            } finally {
                rpcUnderConstruction = false
            }
        }
    }
}


expect suspend fun createSdkClient(
    host: String,
    port: Int,
    path: String,
): Client
