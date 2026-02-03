package presentation.gateway.ktorKrpc

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import y9to.api.krpc.AuthRpc
import y9to.api.krpc.FileRpc
import y9to.api.krpc.MainRpc
import y9to.api.krpc.PostRpc
import y9to.api.krpc.UserRpc


fun Application.krpcApiModule(rpc: MainRpc) {
    install(Krpc) {
        serialization {
            json()
        }
    }

    routing {
        rpc("/api") {
            registerService<AuthRpc> { rpc.auth }
            registerService<UserRpc> { rpc.user }
            registerService<PostRpc> { rpc.post }
            registerService<FileRpc> { rpc.file }
        }
    }
}
