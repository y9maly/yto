package y9to.sdk.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import y9to.libs.stdlib.delegates.static
import y9to.sdk.ClientBuilder
import kotlin.coroutines.cancellation.CancellationException


interface ClientGracefulScope {
    val backgroundScope: CoroutineScope
    val foregroundScope: CoroutineScope
}

fun ClientGracefulScope(
    backgroundScope: CoroutineScope,
    foregroundScope: CoroutineScope
) = object : ClientGracefulScope {
    override val backgroundScope = backgroundScope
    override val foregroundScope = foregroundScope

    init {
        foregroundScope.coroutineContext.job.invokeOnCompletion { cause ->
            if (!backgroundScope.isActive)
                return@invokeOnCompletion

            println("WARNING: " + when (cause) {
                null -> "foregroundScope was completed normally before backgroundScope is completed"
                is CancellationException -> "foregroundScope was cancelled before backgroundScope is completed"
                else -> "foregroundScope was completed exceptionally before backgroundScope is completed. cause: $cause"
            })

            backgroundScope.cancel("backgroundScope cannot be active while foregroundScope is cancelled")
        }
    }
}

val ClientExtensionKeys.GracefulScope by static {
    ClientExtensionKey<ClientGracefulScope>("GracefulScope")
}

fun ClientBuilder.installGracefulScope(
    backgroundScope: CoroutineScope,
    foregroundScope: CoroutineScope,
) {
    extensions.override(ClientExtensionKeys.GracefulScope, ClientGracefulScope(
        backgroundScope = backgroundScope,
        foregroundScope = foregroundScope,
    ))
}

val ExtensibleClient.backgroundScope
    get() = extensions.require(ClientExtensionKeys.GracefulScope).backgroundScope
val ExtensibleClient.foregroundScope
    get() = extensions.require(ClientExtensionKeys.GracefulScope).foregroundScope

