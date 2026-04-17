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
import y9to.api.types.RefreshToken
import y9to.api.types.SessionId
import y9to.api.types.Token
import y9to.sdk.internals.ACCESS_TOKEN_KEY
import y9to.sdk.internals.REFRESH_TOKEN_KEY
import y9to.sdk.internals.RequestController
import y9to.sdk.internals.RpcClientController
import y9to.sdk.internals.RpcController
import y9to.sdk.internals.awaitRpc
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


actual suspend fun createSdkClient(
    host: String,
    port: Int,
    path: String,
    kvStorage: KVStorage,
): Client {
    val scope = CoroutineScope(SupervisorJob())

    val httpClient = HttpClient(CIO) {
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
    )
}
