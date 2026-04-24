package presentation.workers.updatePublisher

import backend.core.types.ClientId
import backend.core.types.SessionId
import domain.event.AuthStateChanged
import domain.event.Event
import domain.event.PostContentEdited
import domain.event.UserEdited
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.y9san9.aqueue.AQueue
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.presenter.PresenterCollection
import presentation.updateProducer.UpdateProducer
import presentation.updateSubscriptionsStore.UpdateEvent
import presentation.updateSubscriptionsStore.UpdateSubscriptionsStore
import y9to.api.types.Update
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi


@OptIn(ExperimentalAtomicApi::class)
class UpdatePublisherDefault(
    internal val eventSource: EventSource,
    internal val producer: UpdateProducer,
    internal val updateSubscriptionsStore: UpdateSubscriptionsStore,
    internal val presenter: PresenterCollection,
    internal val sessionProvider: SessionProvider,
) : UpdatePublisher {
    companion object {
        internal val logger = KotlinLogging.logger {}
    }

    internal val sessionAQueue = AQueue()
    internal val clientAQueue = AQueue()

    private var started = AtomicBoolean(false)
    override suspend fun start(): Nothing {
        if (!started.compareAndSet(expectedValue = false, newValue = true)) {
            logger.warn { "UpdatePublisherDefault.start() called twice" }
            error("Publisher already started")
        }

        try {
            startImpl()
        } catch (t: Throwable) {
            logger.error(t) { "startImpl() finished with exception" }
            throw t
        }
    }
}

private suspend fun UpdatePublisherDefault.startImpl(): Nothing = eventSource.collectWithCoroutineScope { event ->
    when (event) {
        is AuthStateChanged -> withSession(event.session) {
            val authState = presenter.AuthState(event.authState)
            producer.emit(sessionId, Update.AuthStateChanged(authState))
        }

        is UserEdited -> {
            launch {
                // todo Для всех сессий результат `presenter.User` будет одинаков
                withClientSessions(event.user.id) {
                    val user = presenter.User(event.user)
                    producer.emit(sessionId, Update.UserEdited(user))
                }
            }

            updateSubscriptionsStore.getSubscribers(UpdateEvent.UserEdited(event.user.id)).forEach { subscriber ->
                launch {
                    withSession(subscriber) {
                        val user = presenter.User(event.user)
                        producer.emit(sessionId, Update.UserEdited(user))
                    }
                }
            }
        }

        is PostContentEdited -> {
            updateSubscriptionsStore.getSubscribers(UpdateEvent.PostContentEdited(event.postId)).forEach { subscriber ->
                launch {
                    withSession(subscriber) {
                        val postId = presenter.post.PostId(event.postId)
                        val newContent = presenter.PostContent(event.newContent)
                        producer.emit(sessionId, Update.PostContentEdited(postId, newContent))
                    }
                }
            }
        }
    }
}

private suspend fun UpdatePublisherDefault.withClientSessions(client: ClientId, block: suspend context(Context) () -> Unit) {
    clientAQueue.execute(client) {
        coroutineScope {
            sessionProvider.client(client).forEach { session ->
                launch {
                    withSession(session, block)
                }
            }
        }
    }
}

private suspend fun UpdatePublisherDefault.withSession(sessionId: SessionId, block: suspend context(Context) () -> Unit) {
    sessionAQueue.execute(sessionId.long) {
        withSessionContext(sessionId) {
            block()
        }
    }
}

private inline fun withSessionContext(sessionId: SessionId, block: context(Context) () -> Unit) {
    context(Context()) {
        presentation.integration.context.elements.sessionId = sessionId
        block()
    }
}

private suspend fun EventSource.collectWithCoroutineScope(
    collector: suspend CoroutineScope.(value: Event) -> Unit
): Nothing = collect {
    coroutineScope {
        collector(it)
    }
}
