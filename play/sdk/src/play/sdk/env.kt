package play.sdk

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.encodedPath
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import y9to.api.krpc.MainRpc
import y9to.api.types.SessionId
import y9to.api.types.Token


val httpClient = HttpClient(CIO) {
    install(WebSockets)
    installKrpc {
        serialization {
            json()
        }
    }
}

val rpcClient = httpClient.rpc {
    url {
        this.host = "localhost"
        this.port = 8103
        encodedPath = "/api"
    }
}

val rpc = MainRpc(
    rpcClient.withService(),
    rpcClient.withService(),
    rpcClient.withService(),
)

val token_invalid = Token(Token.Unsafe(SessionId(0), ""))
val token_1 = Token(Token.Unsafe(SessionId(1), "0.0.1")) // rpc.auth.createSession()
val token_2 = Token(token_1.unsafe.copy(session = SessionId(2)))
val token_3 = Token(token_1.unsafe.copy(session = SessionId(3)))
val token_4 = Token(token_1.unsafe.copy(session = SessionId(4)))
val token_5 = Token(token_1.unsafe.copy(session = SessionId(5)))
val token_6 = Token(token_1.unsafe.copy(session = SessionId(6)))
