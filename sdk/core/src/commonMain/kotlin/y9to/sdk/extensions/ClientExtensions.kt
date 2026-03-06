package y9to.sdk.extensions


interface ClientExtensions {
    fun <E : Any> getOrNull(key: ClientExtensionKey<E>): E?
    fun <E : Any> require(key: ClientExtensionKey<E>): E
    fun <E : Any> override(key: ClientExtensionKey<E>, extension: E)
}

@Suppress("UNCHECKED_CAST")
internal class ClientExtensionsImpl : ClientExtensions {
    private val map = mutableMapOf<ClientExtensionKey<*>, Any>()

    override fun <E : Any> getOrNull(key: ClientExtensionKey<E>): E? {
        return map[key] as E?
    }

    override fun <E : Any> require(key: ClientExtensionKey<E>): E {
        return getOrNull(key) ?: error("Required extension are not registered '$key'")
    }

    override fun <E : Any> override(key: ClientExtensionKey<E>, extension: E) {
        map[key] = extension
    }
}
