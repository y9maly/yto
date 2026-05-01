package y9to.api.controller

import presentation.assembler.AssemblerCollection
import presentation.assembler.resolve
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.presenter.PresenterCollection
import presentation.updateProvider.UpdateProvider
import presentation.updateSubscriptionsStore.UpdateSubscription
import presentation.updateSubscriptionsStore.UpdateSubscriptionsStore
import y9to.api.types.ApiUpdateSubscription
import y9to.api.types.Update


class UpdateControllerDefault(
    override val assembler: AssemblerCollection,
    override val presenter: PresenterCollection,
    private val updateProvider: UpdateProvider,
    private val updateSubscriptionsStore: UpdateSubscriptionsStore,
) : UpdateController, ControllerDefault {
    context(_: Context)
    override suspend fun await(): List<Update> {
        val update = updateProvider.await(sessionId)
        return listOf(update)
    }

    context(_: Context)
    override suspend fun consume(count: Int) {
        repeat(count) {
            updateProvider.consume(sessionId)
        }
    }

    context(_: Context)
    override suspend fun subscribe(subscription: ApiUpdateSubscription): Unit = context {
        updateSubscriptionsStore.subscribe(sessionId, subscription.map() ?: return)
    }

    context(_: Context)
    override suspend fun unsubscribe(subscription: ApiUpdateSubscription): Unit = context {
        updateSubscriptionsStore.unsubscribe(sessionId, subscription.map() ?: return)
    }
}

context(_: Context, _: AssemblerCollection)
private suspend fun ApiUpdateSubscription.map(): UpdateSubscription? = when (this) {
    is ApiUpdateSubscription.UserEdited -> UpdateSubscription.UserEdited(user.resolve() ?: return null)
    is ApiUpdateSubscription.PostEdited -> UpdateSubscription.PostEdited(post.resolve() ?: return null)
}
