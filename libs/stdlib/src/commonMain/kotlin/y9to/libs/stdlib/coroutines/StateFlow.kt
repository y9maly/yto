package y9to.libs.stdlib.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


private object MapInNull

fun <T, R> StateFlow<T>.mapIn(scope: CoroutineScope, transform: (T) -> R): StateFlow<R> {
    val flow = MutableStateFlow<Any?>(MapInNull)

    scope.launch(start = CoroutineStart.UNDISPATCHED) {
        collect {
            flow.value = transform(it)
        }
    }

    // На практике 'MapInNull == flow.value' должно быть невозможно.
    // Но в теории это может произойти либо когда изменится поведение UNDISPATCHED,
    // либо когда изменится поведение StateFlow.collect.
    // Просто, чтобы быть уверенным что .mapIn никогда не сломается, пусть здесь будет обработка этого случая
    if (MapInNull == flow.value)
        flow.value = transform(this.value)

    return flow as StateFlow<R>
}
