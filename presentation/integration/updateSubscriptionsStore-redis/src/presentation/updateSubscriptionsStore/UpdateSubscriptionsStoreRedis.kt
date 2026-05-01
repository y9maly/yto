package presentation.updateSubscriptionsStore

import backend.core.types.SessionId
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import kotlinx.serialization.json.Json
import y9to.api.types.ClientId


@OptIn(ExperimentalLettuceCoroutinesApi::class)
class UpdateSubscriptionsStoreRedis(
    private val commands: RedisCoroutinesCommands<String, String>,
) : UpdateSubscriptionsStore {
    override suspend fun subscribe(
        session: SessionId,
        subscription: UpdateSubscription,
    ) {
        val subscriptionString = Json.encodeToString(subscription)

        val events = subscription.getSubscribedEvents()

        commands.sadd("update:subscriptions-${session.long}", subscriptionString)
        events.forEach { event ->
            val eventStringId = event.stringId()
            commands.sadd("update:eventSubscribers-$eventStringId", session.long.toString())
        }
    }

    override suspend fun unsubscribe(
        session: SessionId,
        subscription: UpdateSubscription
    ) {
        val subscriptionString = Json.encodeToString(subscription)

        val events = subscription.getSubscribedEvents()

        commands.srem("update:subscriptions-${session.long}", subscriptionString)
        events.forEach { event ->
            val eventStringId = event.stringId()
            commands.srem("update:eventSubscribers-$eventStringId", session.long.toString())
        }
    }

    override suspend fun getSubscribers(forUpdateEvent: UpdateEvent): Set<SessionId> {
        val eventStringId = forUpdateEvent.stringId()
        return commands.smembers("update:eventSubscribers-$eventStringId")
            .map { SessionId(it.toLong()) }
            .toSet()
    }
}

private fun UpdateEvent.stringId() = when (this) {
    is UpdateEvent.UserEdited -> "user:$user"
    is UpdateEvent.PostEdited -> "post:$post"
}

private fun UpdateSubscription.getSubscribedEvents(): Set<UpdateEvent> = when (this) {
    is UpdateSubscription.UserEdited -> setOf(UpdateEvent.UserEdited(user))
    is UpdateSubscription.PostEdited -> setOf(UpdateEvent.PostEdited(post))
}
