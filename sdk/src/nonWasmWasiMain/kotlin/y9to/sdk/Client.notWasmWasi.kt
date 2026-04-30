package y9to.sdk

import io.ktor.client.*
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.serialization.json.json
import y9to.sdk.internals.ACCESS_TOKEN_KEY
import y9to.sdk.internals.REFRESH_TOKEN_KEY
import y9to.sdk.internals.RequestController
import y9to.sdk.internals.RpcClientController
import y9to.sdk.internals.RpcController
import y9to.sdk.internals.awaitRpc
import kotlin.time.Duration.Companion.milliseconds


internal expect val HttpClientEngine: HttpClientEngineFactory<HttpClientEngineConfig>

actual suspend fun createSdkClient(
    useHttps: Boolean,
    host: String,
    port: Int,
    path: String,
    kvStorage: KVStorage,
    clientLogger: ClientLogger,
): Client {
    val scope = CoroutineScope(SupervisorJob())

    val httpClient = HttpClient(HttpClientEngine) {
        install(WebSockets)
        installKrpc {
            serialization {
                json()
            }
        }
    }

    val rpcClientController = RpcClientController(
        scope = scope,
        rpcClientFactory = {
            httpClient.rpc {
                url {
                    if (useHttps)
                        this.protocol = URLProtocol.WSS
                    else
                        this.protocol = URLProtocol.WS
                    this.host = host
                    this.port = port
                    encodedPath = path
                }
            }
        },
    )

    val rpcController = RpcController(
        scope = scope,
        rpcClientController = rpcClientController,
    )

    while (kvStorage.getString(REFRESH_TOKEN_KEY) == null) {
        try {
            val (refreshToken, accessToken) = rpcController.awaitRpc().auth.createSession()
            kvStorage.put(REFRESH_TOKEN_KEY, refreshToken.string)
            kvStorage.put(ACCESS_TOKEN_KEY, accessToken.string)
        } catch (e: Exception) {
            e.printStackTrace()
            delay(1000.milliseconds)
        }
    }

    val requestController = RequestController(
        scope = scope,
        kvStorage = kvStorage,
        rpcController = rpcController,
    )

    return Client(
        scope = scope,
        kvStorage = kvStorage,
        httpClient = httpClient,
        requestController = requestController,
        rpcController = rpcController,
        clientLogger = clientLogger,
    )
}
