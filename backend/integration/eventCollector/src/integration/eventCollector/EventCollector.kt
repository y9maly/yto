package integration.eventCollector

import domain.event.Event


interface EventCollector {
    fun emit(event: Event)
}
