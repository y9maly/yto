package presentation.api.krpc

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.updateProvider.UpdateProvider
import y9to.api.krpc.UpdateRpc
import y9to.api.types.Token
import y9to.api.types.Update
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds


class UpdateRpcDefault(
    private val authenticator: Authenticator,
    private val updateProvider: UpdateProvider,
) : UpdateRpc {
    override fun receive(token: Token): Flow<Update> = channelFlow {
        while (true) {
            authenticate(token) {
                val update = updateProvider.await(sessionId)
                send(update)
                updateProvider.consume(sessionId)
            }
        }
    }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context) () -> R) =
        presentation.api.krpc.internals.authenticate(authenticator, token) { block(this) }
}
