package y9to.sdk

import y9to.sdk.extensions.ExtensibleClient


interface ClientBuilder : ExtensibleClient {
    val client: Client
}


internal class ClientBuilderImpl(
    override val client: Client
) : ClientBuilder {
    override val extensions = client.extensions
}

fun Client(configure: ClientBuilder.() -> Unit = {}): Client {
    val client = ClientImpl()
    configure(ClientBuilderImpl(client))
    return client
}
