package y9to.sdk.extensions

import y9to.api.y9to.api.endpoint.AuthEndpoint
import y9to.api.y9to.api.endpoint.FileEndpoint
import y9to.api.y9to.api.endpoint.PostEndpoint
import y9to.api.y9to.api.endpoint.UserEndpoint
import y9to.libs.stdlib.delegates.static
import y9to.sdk.ClientBuilder


data class ClientEndpoints(
    val auth: AuthEndpoint,
    val file: FileEndpoint,
    val post: PostEndpoint,
    val user: UserEndpoint,
)


val ClientExtensionKeys.Endpoints by static {
    ClientExtensionKey<ClientEndpoints>("ClientEndpoints")
}

val ExtensibleClient.endpoints
    get() = extensions.require(ClientExtensionKeys.Endpoints)

fun ClientBuilder.installEndpoints(endpoints: ClientEndpoints) {
    extensions.override(ClientExtensionKeys.Endpoints, endpoints)
}
