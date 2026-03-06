package y9to.sdk.extensions

import y9to.libs.stdlib.delegates.static


internal data class AllFeatures(
    val auth: AuthFeature,
)

internal val ClientExtensionKeys.Features by static {
    ClientExtensionKey<AllFeatures>("AllFeatures")
}

internal val ExtensibleClient.all
    get() = extensions.require(ClientExtensionKeys.Features)


val ExtensibleClient.auth get() = all.auth
