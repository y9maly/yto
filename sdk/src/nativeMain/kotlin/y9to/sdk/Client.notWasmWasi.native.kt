package y9to.sdk

import io.ktor.client.engine.cio.CIO

internal actual val HttpClientEngine: io.ktor.client.engine.HttpClientEngineFactory<io.ktor.client.engine.HttpClientEngineConfig>
    get() = CIO
