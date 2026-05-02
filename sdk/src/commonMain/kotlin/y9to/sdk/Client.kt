package y9to.sdk

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import io.github.oshai.kotlinlogging.Marker
import kotlinx.coroutines.CoroutineScope
import y9to.sdk.internals.RequestController
import y9to.sdk.internals.RpcController
import y9to.sdk.internals.UpdateCenter


class Client internal constructor(
    internal val scope: CoroutineScope,
    internal val kvStorage: KVStorage,
    internal val httpClient: Any,
    internal val rpcController: RpcController,
    private val clientLogger: ClientLogger,
) {
    private val cachedLoggers = mutableMapOf<String, KLogger>()

    internal val updateCenter = UpdateCenter(this)
    internal val requestController = RequestController(scope, kvStorage, rpcController, logger("RequestController"))

    val auth = AuthClient(this)
    val user = UserClient(this)
    val post = PostClient(this)
    val file = FileClient(this)

    internal fun logger(componentName: String): KLogger = cachedLoggers[componentName] ?: object : KLogger {
        override val name = clientLogger.name

        override fun isLoggingEnabledFor(level: Level, marker: Marker?): Boolean {
            return clientLogger.isLoggingEnabledFor(componentName, level, marker)
        }

        override fun at(level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
            clientLogger.at(componentName, level, marker, block)
        }
    }.also { cachedLoggers[componentName] = it }
}


expect suspend fun createSdkClient(
    useHttps: Boolean,
    host: String,
    port: Int,
    path: String,
    kvStorage: KVStorage,
    clientLogger: ClientLogger = EmptyClientLogger,
): Client
