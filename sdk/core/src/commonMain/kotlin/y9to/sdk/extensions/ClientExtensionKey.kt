package y9to.sdk.extensions


interface ClientExtensionKeys {
    companion object Keys : ClientExtensionKeys
}

interface ClientExtensionKey<E : Any>


private class NamedClientExtensionKey<E : Any>(val name: String) : ClientExtensionKey<E> {
    override fun toString() = "ClientExtensionKey(name=$name)"
}

private class UnnamedClientExtensionKey<E : Any> : ClientExtensionKey<E>

fun <E : Any> ClientExtensionKey(name: String? = null): ClientExtensionKey<E> {
    return if (name != null) NamedClientExtensionKey(name) else UnnamedClientExtensionKey()
}
