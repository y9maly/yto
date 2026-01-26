package y9to.sdk

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.encodedPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import y9to.api.krpc.MainRpc
import y9to.api.types.SessionId
import y9to.api.types.Token


actual suspend fun createSdkClient(host: String, port: Int, path: String): y9to.sdk.Client {
    val httpClient = HttpClient(CIO) {
        install(WebSockets)
        installKrpc {
            serialization {
                json()
            }
        }
    }

    var currentRpc: Pair<Job, MainRpc>? = null
    val rpcFactory = suspend suspend@{
        val current = currentRpc
        if (current != null && current.first.isActive)
            return@suspend current
        currentRpc = null

        val rpcClient = httpClient.rpc {
            url {
                this.host = "localhost"
                this.port = 8103
                encodedPath = "/api"
            }
        }

        val job = Job().let { job ->
            GlobalScope.launch {
                rpcClient.webSocketSession.await().coroutineContext.job.join()
                rpcClient.awaitCompletion()
                job.cancel()
            }
        }

        val rpc = MainRpc(
            rpcClient.withService(),
            rpcClient.withService(),
            rpcClient.withService(),
        )

        currentRpc = job to rpc
        return@suspend job to rpc
    }

    val rpc = rpcFactory().second

    val token = Token(Token.Unsafe(SessionId(2), "0.0.1"))
    val session = rpc.auth.getSession(token)
    val scope = CoroutineScope(SupervisorJob())
    return Client(token, session, scope, rpcFactory)
}
