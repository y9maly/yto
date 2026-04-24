package presentation.workers.updatePublisher

import domain.event.AuthStateChanged
import domain.event.Event
import domain.event.PostContentEdited
import domain.event.PostCreated
import domain.event.PostDeleted
import domain.event.SessionCreated
import domain.event.UserEdited
import domain.event.UserRegistered
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.common.TopicPartition
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


class EventSourceKafka(
    private val scope: CoroutineScope,
    private val consumer: Consumer<String, String>,
) : EventSource {
    companion object {
        internal val logger = KotlinLogging.logger {}
    }

    private val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE)

    private val json = Json { }

    override suspend fun collect(collector: FlowCollector<Event>): Nothing {
        events.collect(collector)
    }

    init {
        Thread.startVirtualThread {
            try {
                consumeLoop(scope.coroutineContext.job)
            } catch (t: Throwable) {
                logger.error(t) { "Exception in consumeLoop" }
            } finally {
                consumer.close()
            }
        }
    }

    private fun consumeLoop(job: Job) {
        consumer.subscribe(
            listOf(
                "event.session_created",
                "event.auth_state_changed",
                "event.post_created",
                "event.post_content_edited",
                "event.post_deleted",
                "event.user_registered",
                "event.user_edited",
            )
        )

        while (job.isActive) {
            val records = consumer.poll(1.seconds.toJavaDuration()).toList()

            if (records.isNotEmpty()) {
                logger.debug {
                    val recordsByTopic = records.groupBy { it.topic() }
                    buildString {
                        append("Received events: ")
                        recordsByTopic.forEach { (topic, records) ->
                            val recordsString = records.joinToString { "'${it.value()}'" }
                            append("[$topic] $recordsString\n")
                        }
                        deleteAt(lastIndex) // remove last \n
                    }
                }
            }

            for (record in records) {
                val event = try {
                    decode(record.topic(), record.value())
                } catch (e: Exception) {
                    logger.warn(e) { "Can't decode record (topic=${record.topic()}, value='${record.value()}')" }
                    continue
                }

                check(events.tryEmit(event)) { "tryEmit can't be false because events.extraBufferCapacity == Int.MAX_VALUE" }
            }
        }
    }

    private fun decode(topic: String, payload: String): Event {
        return when (topic) {
            "event.session_created" ->
                json.decodeFromString<SessionCreated>(payload)

            "event.auth_state_changed" ->
                json.decodeFromString<AuthStateChanged>(payload)

            "event.post_created" ->
                json.decodeFromString<PostCreated>(payload)

            "event.post_content_edited" ->
                json.decodeFromString<PostContentEdited>(payload)

            "event.post_deleted" ->
                json.decodeFromString<PostDeleted>(payload)

            "event.user_registered" ->
                json.decodeFromString<UserRegistered>(payload)

            "event.user_edited" ->
                json.decodeFromString<UserEdited>(payload)

            else -> error("Unknown topic: $topic")
        }
    }
}
