package presentation.api.krpc.internals

import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import y9to.api.types.Token


internal suspend inline fun <R> authenticate(
    authenticator: Authenticator,
    token: Token,
    block: Context.() -> R
): R {
    val context = Context {
        sessionId = authenticator.authenticate(token)
    }
    return block(context)
}
