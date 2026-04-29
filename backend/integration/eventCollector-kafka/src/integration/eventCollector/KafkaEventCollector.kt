package integration.eventCollector

import domain.event.AuthStateChanged
import domain.event.Event
import domain.event.LoginStateChanged
import domain.event.PostContentEdited
import domain.event.PostCreated
import domain.event.PostDeleted
import domain.event.SessionCreated
import domain.event.UserEdited
import domain.event.UserRegistered
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import y9to.libs.stdlib.coroutines.flow.collectIn


@OptIn(ExperimentalSerializationApi::class)
class KafkaEventCollector(
    private val scope: CoroutineScope,
    private val producer: Producer<String, String>,
) : EventCollector {
    companion object {
        internal val logger = KotlinLogging.logger { }
    }

    private val queue = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE)

    private val json = Json {}

    init {
        queue.collectIn(scope) {
            send(it)
        }
    }

    override fun emit(event: Event) {
        check(queue.tryEmit(event))
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun send(event: Event) {
        var topic: String
        var key: String? = null
        var payload: String
        when (event) {
            is SessionCreated -> {
                topic = "event.session_created"
                key = event.session.id.long.toString()
                payload = json.encodeToString(event)
            }

            is AuthStateChanged -> {
                topic = "event.auth_state_changed"
                key = event.session.long.toString()
                payload = json.encodeToString(event)
            }

            is LoginStateChanged -> {
                topic = "event.login_state_changed"
                key = event.session.long.toString()
                payload = json.encodeToString(event)
            }

            is PostCreated -> {
                topic = "event.post_created"
                key = event.post.id.long.toString()
                payload = json.encodeToString(event)
            }

            is PostContentEdited -> {
                topic = "event.post_content_edited"
                key = event.postId.long.toString()
                payload = json.encodeToString(event)
            }

            is PostDeleted -> {
                topic = "event.post_deleted"
                key = event.postId.long.toString()
                payload = json.encodeToString(event)
            }

            is UserRegistered -> {
                topic = "event.user_registered"
                key = event.user.id.long.toString()
                payload = json.encodeToString(event)
            }

            is UserEdited -> {
                topic = "event.user_edited"
                key = event.user.id.long.toString()
                payload = json.encodeToString(event)
            }

            else -> TODO()
        }

        val record =
            if (key != null) ProducerRecord(topic, key, payload)
            else ProducerRecord(topic, payload)

        producer.send(record) { metadata, exception ->
            if (exception != null) {
                logger.error(exception) { "Cannot deliver record to kafka: $record" }
            }
        }
    }
}
