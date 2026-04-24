package presentation.workers.updatePublisher

import backend.core.types.ClientId
import backend.core.types.SessionId
import domain.event.AuthStateChanged
import domain.event.UserEdited
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.y9san9.aqueue.AQueue
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.presenter.PresenterCollection
import presentation.updateProducer.UpdateProducer
import y9to.api.types.Update
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi


@OptIn(ExperimentalAtomicApi::class)
class UpdatePublisherDefault(
    internal val eventSource: EventSource,
    internal val producer: UpdateProducer,
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

private suspend fun UpdatePublisherDefault.startImpl(): Nothing = eventSource.collect { event ->
    when (event) {
        is AuthStateChanged -> withSession(event.session) {
            val authState = presenter.AuthState(event.authState)
            producer.emit(sessionId, Update.AuthStateChanged(authState))
        }

        // todo Для всех сессий результат `presenter.User` будет одинаков
        is UserEdited -> withClientSessions(event.user.id) {
            val user = presenter.User(event.user)
            producer.emit(sessionId, Update.UserEdited(user))
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
