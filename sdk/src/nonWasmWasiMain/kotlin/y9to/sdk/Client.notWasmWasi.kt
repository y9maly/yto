package y9to.sdk

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import y9to.api.krpc.MainRpc
import y9to.api.types.SessionId
import y9to.api.types.Token
import kotlin.time.Duration.Companion.seconds


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
            rpcClient.withService(),
        )

        currentRpc = job to rpc
        return@suspend job to rpc
    }

    val rpc = rpcFactory().second

    val token = Token(Token.Unsafe(SessionId(2), "0.0.1"))
    val session = rpc.auth.getSession(token)
    val scope = CoroutineScope(SupervisorJob())
    return Client(token, session, httpClient, scope, rpcFactory)
}
