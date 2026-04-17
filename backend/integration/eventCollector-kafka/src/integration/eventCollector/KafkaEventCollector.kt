package integration.eventCollector

import domain.event.AuthStateChanged
import domain.event.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import y9to.libs.stdlib.coroutines.flow.collectIn

@OptIn(ExperimentalSerializationApi::class)
class KafkaEventCollector(
    private val scope: CoroutineScope,
    private val producer: Producer<String, String>,
) : EventCollector {
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
            is AuthStateChanged -> {
                topic = "event.auth_state_changed"
                key = event.session.long.toString()
                payload = json.encodeToString(event)
            }

            else -> TODO()
        }

        val record =
            if (key != null) ProducerRecord(topic, key, payload)
            else ProducerRecord(topic, payload)

        producer.send(record) { metadata, exception ->
            // todo log exception
            exception?.printStackTrace()
        }
    }
}
