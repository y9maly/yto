package presentation.api.krpc.internals

import presentation.authenticator.Authenticator
import presentation.authenticator.authenticateOrThrow
import presentation.integration.context.Context
import presentation.integration.context.elements.authState
import presentation.integration.context.elements.sessionId
import y9to.api.types.Token
import y9to.libs.stdlib.optional.onPresent


internal suspend inline fun <R> authenticate(
    authenticator: Authenticator,
    token: Token,
    block: Context.() -> R
): R {
    val context = Context {
        val result = authenticator.authenticateOrThrow(token)

        sessionId = result.sessionId
        result.authState.onPresent { authState = it }
    }
    return block(context)
}
