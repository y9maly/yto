package y9to.sdk.internals

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import y9to.libs.stdlib.coroutines.flow.firstNotNull
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalAtomicApi::class)
var nextRequestId = AtomicInt(0)

/**
 * @param debugInfo human-readable information about the request
 * @param isLongRunning true if the request is expected to run indefinitely. Used to prevent periodic warnings in the logs
 */
@OptIn(ExperimentalAtomicApi::class)
internal suspend fun <R> ClientOwner.request(
    debugInfo: String? = null,
    isLongRunning: Boolean = false,
    block: suspend RequestScope.() -> R
): R {
    val logger = client.logger("RequestController")
    val requestId = nextRequestId.fetchAndIncrement()

    var attempt = 1
    while (true) {
        val scope = awaitRequestScope()

        try {
            logger.logExecuting(requestId, debugInfo, attempt)

            return coroutineScope {
                val job = if (isLongRunning) {
                    null
                } else {
                    launch {
                        var counter = 0
                        while (true) {
                            delay(5.seconds)
                            counter++
                            logger.info { "Request $requestId has been running for ${counter * 5}s" }
                        }
                    }
                }

                block(scope)
                    .also { job?.cancel() }
            }
        } catch (t: Throwable) {
            attempt++

            if (isTokenException(t)) {
                logger.trace { "Request $requestId failed: ${t.message}. Invalidating token and retrying..." }
                client.requestController.invalidateAccessToken()
                continue
            }

            if (t is CancellationException) {
                // kotlinx.rpc client throws CancellationException to all active callers when the client is cancelled
                if (currentCoroutineContext().isActive) {
                    logger.trace { "RPC client was cancelled while executing request $requestId. Retrying..." }
                    continue
                } else {
                    logger.trace { "Request $requestId was cancelled" }
                    throw t
                }
            }

            logger.error(t) { "Unknown exception occurred while executing request $requestId." }

            // todo wrap with SdkException or something like that
            throw RuntimeException("Failed to execute request $requestId", t)
        }
    }
}

private fun KLogger.logExecuting(requestId: Int, debugInfo: String?, attempt: Int) {
    trace {
        buildString {
            append("Executing request $requestId")
            if (attempt > 1)
                append(" (attempt $attempt)")
            if (debugInfo != null)
                append(". $debugInfo")
        }
    }
}

private suspend fun ClientOwner.awaitRequestScope(): RequestScope {
    return combine(client.requestController.accessToken, client.rpcController.rpc) { token, rpc ->
        RequestScope(
            token = token ?: return@combine null,
            rpc = rpc ?: return@combine null,
        )
    }.firstNotNull()
}

// todo Временно
private fun isTokenException(t: Throwable): Boolean {
    return "expired token" in (t.message ?: t.toString()).lowercase() ||
            "invalid token" in (t.message ?: t.toString()).lowercase() ||
            "revoked token" in (t.message ?: t.toString()).lowercase()
}
