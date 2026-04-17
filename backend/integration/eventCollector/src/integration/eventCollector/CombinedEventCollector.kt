package integration.eventCollector

import domain.event.Event


class CombinedEventCollector(
    private val collectors: Array<EventCollector>,
) : EventCollector {
    override fun emit(event: Event) {
        collectors.forEach { it.emit(event) }
    }
}
