package presentation.workers.updatePublisher

import domain.event.Event
import kotlinx.coroutines.flow.FlowCollector


interface EventSource {
    suspend fun collect(collector: FlowCollector<Event>): Nothing
}
