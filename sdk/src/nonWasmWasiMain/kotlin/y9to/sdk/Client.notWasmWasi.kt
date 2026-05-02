package y9to.sdk

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.github.oshai.kotlinlogging.Level
import io.github.oshai.kotlinlogging.Marker
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
        logger = object : KLogger {
            override val name = clientLogger.name

            override fun isLoggingEnabledFor(level: Level, marker: Marker?): Boolean {
                return clientLogger.isLoggingEnabledFor("RpcClientController", level, marker)
            }

            override fun at(level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
                clientLogger.at("RpcClientController", level, marker, block)
            }
        }
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
        } catch (t: Throwable) {
            clientLogger.at("Initializer", Level.ERROR) {
                message = "Failed to create initial token pair. Retrying in 1 second."
                cause = t
            }

            delay(1000.milliseconds)
        }
    }

    return Client(
        scope = scope,
        kvStorage = kvStorage,
        httpClient = httpClient,
        rpcController = rpcController,
        clientLogger = clientLogger,
    )
}
