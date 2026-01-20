package y9to.libs.stdlib.coroutines.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun <T> Flow<T>.collectIn(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT
) {
    scope.launch(context, start) {
        collect()
    }
}

fun <T> Flow<T>.collectIn(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    collector: FlowCollector<T>
) {
    scope.launch(context, start) {
        collect(collector)
    }
}

fun <T> Flow<T>.collectLatestIn(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    action: suspend (T) -> Unit
) {
    scope.launch(context, start) {
        collectLatest(action)
    }
}

fun <T> Flow<Iterable<T>>.flatCollectIn(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    collector: FlowCollector<T>
) {
    collectIn(scope, context, start) { iterable ->
        for (it in iterable) collector.emit(it)
    }
}

fun <T> Flow<Iterable<T>>.flatCollectLatestIn(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    collector: FlowCollector<T>
) {
    collectLatestIn(scope, context, start) { iterable ->
        for (it in iterable) collector.emit(it)
    }
}


suspend fun <T> Flow<Iterable<T>>.flatCollect(collector: FlowCollector<T>) {
    collect { iterable ->
        for (it in iterable) collector.emit(it)
    }
}

suspend fun <T> Flow<Iterable<T>>.flatCollectLatest(collector: FlowCollector<T>) {
    collectLatest { iterable ->
        for (it in iterable) collector.emit(it)
    }
}

fun <T> Flow<Iterable<T>>.flatten(): Flow<T> = transform { iterable ->
    for (it in iterable) emit(it)
}


suspend fun <T> Flow<T?>.firstNotNull(): T =
    first { it != null } as T

suspend fun <T> Flow<T?>.firstNotNullOrNull(): T? =
    firstOrNull { it != null } as T?

suspend inline fun <reified T> Flow<*>.firstIsInstance(): T =
    filterIsInstance<T>().first()

suspend inline fun <reified T> Flow<*>.firstIsInstanceOrNull(): T? =
    filterIsInstance<T>().firstOrNull()
