package y9to.sdk.extensions

import y9to.sdk.ClientBuilder


fun ClientBuilder.installFeatures() {
    extensions.override(ClientExtensionKeys.Features, AllFeatures(
        auth = AuthFeature(client)
    ))
}
