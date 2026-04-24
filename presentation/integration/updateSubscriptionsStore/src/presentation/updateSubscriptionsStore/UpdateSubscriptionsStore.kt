package presentation.updateSubscriptionsStore

import backend.core.types.SessionId
import kotlin.time.Duration


interface UpdateSubscriptionsStore {
    suspend fun subscribe(session: SessionId, subscription: UpdateSubscription)
    suspend fun unsubscribe(session: SessionId, subscription: UpdateSubscription)
    suspend fun getSubscribers(forUpdateEvent: UpdateEvent): Set<SessionId>
}
