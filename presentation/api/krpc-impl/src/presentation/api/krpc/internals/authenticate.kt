package presentation.api.krpc.internals

import presentation.authenticator.Authenticator
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.sessionId
import y9to.api.types.Token


internal suspend inline fun <R> authenticate(
    authenticator: Authenticator,
    token: Token,
    block: CallContext.() -> R
): R {
    val sessionId = authenticator.authenticate(token)
    val context = CallContext {
        this.sessionId = sessionId
    }
    return block(context)
}
