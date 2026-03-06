package y9to.sdk

import y9to.sdk.extensions.ClientExtensionsImpl
import y9to.sdk.extensions.ExtensibleClient


interface Client : ExtensibleClient


internal class ClientImpl : Client {
    override val extensions = ClientExtensionsImpl()
}
