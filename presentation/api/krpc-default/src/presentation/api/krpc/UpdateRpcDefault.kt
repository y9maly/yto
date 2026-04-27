package presentation.api.krpc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import y9to.api.controller.UpdateController
import y9to.api.krpc.UpdateRpc
import y9to.api.types.ApiUpdateSubscription
import y9to.api.types.Token
import y9to.api.types.Update


class UpdateRpcDefault(
    private val authenticator: Authenticator,
    private val controller: UpdateController,
) : UpdateRpc {
    override fun receive(token: Token): Flow<Update> = channelFlow {
        while (true) {
            authenticate(token) {
                val updates = controller.await()
                updates.forEach { update ->
                    send(update)
                    controller.consume(1)
                }
            }
        }
    }

    override suspend fun subscribe(token: Token, subscription: ApiUpdateSubscription) = authenticate(token) {
        controller.subscribe(subscription)
    }

    override suspend fun unsubscribe(token: Token, subscription: ApiUpdateSubscription) = authenticate(token) {
        controller.unsubscribe(subscription)
    }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context) () -> R) =
        presentation.api.krpc.internals.authenticate(authenticator, token) { block(this) }
}
