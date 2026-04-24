package presentation.gateway.ktorKrpc

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import y9to.api.krpc.*


fun Application.krpcApiModule(rpc: RpcCollection) {
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
            registerService<UpdateRpc> { rpc.update }
        }
    }
}
