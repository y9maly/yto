package presentation.api.krpc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import presentation.assembler.AssemblerCollection
import presentation.assembler.map
import presentation.assembler.resolve
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.updateProvider.UpdateProvider
import presentation.updateSubscriptionsStore.UpdateSubscription
import presentation.updateSubscriptionsStore.UpdateSubscriptionsStore
import y9to.api.krpc.UpdateRpc
import y9to.api.types.ApiUpdateSubscription
import y9to.api.types.Token
import y9to.api.types.Update


class UpdateRpcDefault(
    private val assembler: AssemblerCollection,
    private val authenticator: Authenticator,
    private val updateProvider: UpdateProvider,
    private val updateSubscriptionsStore: UpdateSubscriptionsStore,
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

    override suspend fun subscribe(token: Token, subscription: ApiUpdateSubscription) = authenticate(token) {
        updateSubscriptionsStore.subscribe(sessionId, subscription.map() ?: return@authenticate)
    }

    override suspend fun unsubscribe(token: Token, subscription: ApiUpdateSubscription) = authenticate(token) {
        updateSubscriptionsStore.unsubscribe(sessionId, subscription.map() ?: return@authenticate)
    }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context, AssemblerCollection) () -> R) =
        presentation.api.krpc.internals.authenticate(authenticator, token) { block(this, assembler) }
}

context(_: Context, _: AssemblerCollection)
private suspend fun ApiUpdateSubscription.map(): UpdateSubscription? = when (this) {
    is ApiUpdateSubscription.UserEdited -> UpdateSubscription.UserEdited(user.resolve() ?: return null)
    is ApiUpdateSubscription.PostContentEdited -> UpdateSubscription.PostContentEdited(post.resolve() ?: return null)
}
