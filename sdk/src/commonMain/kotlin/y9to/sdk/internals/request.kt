package y9to.sdk.internals

import kotlinx.coroutines.flow.combine
import y9to.libs.stdlib.coroutines.flow.firstNotNull


internal suspend fun <R> ClientOwner.request(
    block: suspend RequestScope.() -> R
): R {
    while (true) {
        val scope = awaitRequestScope()

        try {
            return block(scope)
        } catch (e: Exception) {
            if (isTokenException(e)) {
                client.requestController.invalidateAccessToken()
                continue
            }

            throw e
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
private fun isTokenException(e: Exception): Boolean {
    return "expired token" in e.toString().lowercase() ||
            "invalid token" in e.toString().lowercase() ||
            "revoked token" in e.toString().lowercase()
}
