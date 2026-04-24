package container.monolith

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.plus
import org.apache.kafka.clients.consumer.CloseOptions
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import presentation.updateProducer.UpdateProducerDefault
import presentation.workers.updatePublisher.EventSourceKafka
import presentation.workers.updatePublisher.SessionProviderService
import presentation.workers.updatePublisher.UpdatePublisher
import presentation.workers.updatePublisher.UpdatePublisherDefault
import sun.misc.Signal
import java.util.Properties
import kotlin.concurrent.thread


suspend fun Monolith.startWorkers(
    kafkaUrl: String = MonolithDefaults.kafkaUrl
): Nothing = coroutineScope {
    launch {
        UpdatePublisherDefault(
            eventSource = EventSourceKafka(
                scope = CoroutineScope(newSingleThreadContext("TODO")),
                consumer = KafkaConsumer(
                    Properties().apply {
                        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl)
                        put(ConsumerConfig.GROUP_ID_CONFIG, "updatePublisher")
                    },
                    StringDeserializer(),
                    StringDeserializer(),
                ),
            ),
            producer = UpdateProducerDefault(updateManager),
            presenter = presenter,
            sessionProvider = SessionProviderService(service.auth),
        ).start()
    }

    awaitCancellation()
}
