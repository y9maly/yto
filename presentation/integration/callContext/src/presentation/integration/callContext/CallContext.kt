package presentation.integration.callContext

import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.getOrElse
import y9to.libs.stdlib.optional.none
import y9to.libs.stdlib.optional.present


interface CallContext {
    interface Keys {
        companion object : Keys
    }

    companion object : Keys {
        fun <V> Key(): Key<V> = object : Key<V> {}
        fun ContextMap(): ContextMap = ContextMapImpl()
    }

    interface Key<out V>

    interface ContextMap : Iterable<Pair<Key<*>, Any?>> {
        fun <V> getOrNone(key: Key<V>): Optional<V>
        operator fun contains(key: Key<*>): Boolean
        operator fun <V> set(key: Key<V>, value: V)
        fun delete(key: Key<*>)

        override fun iterator(): Iterator<Pair<Key<*>, Any?>>

        operator fun <V> get(key: Key<V>): V = getOrNone(key)
            .getOrElse { error("No such context element: $key") }
        fun <V> getOrNull(key: Key<V>): V? = getOrNone(key).getOrNull()
    }

    val contextMap: ContextMap
}

inline fun CallContext(build: CallContext.() -> Unit): CallContext {
    return CallContext().apply(build)
}

fun CallContext(contextMap: CallContext.ContextMap = CallContext.ContextMap()): CallContext {
    return CallContextImpl(contextMap)
}

private class CallContextImpl(
    override val contextMap: CallContext.ContextMap,
) : CallContext

private class ContextMapImpl : CallContext.ContextMap {
    private val map = mutableMapOf<CallContext.Key<*>, Any?>()
    private val lock = Any()

    @Suppress("UNCHECKED_CAST")
    override fun <V> getOrNone(key: CallContext.Key<V>): Optional<V> = synchronized(lock) {
        val value = map[key]
        if (value == null && !map.containsKey(key))
            return@synchronized none<V>()
        present(value as V)
    }

    override fun contains(key: CallContext.Key<*>): Boolean = synchronized(lock) {
        return map.containsKey(key)
    }

    override fun <V> set(key: CallContext.Key<V>, value: V): Unit = synchronized(lock) {
        map[key] = value
    }

    override fun delete(key: CallContext.Key<*>): Unit = synchronized(lock) {
        map.remove(key)
    }

    override fun iterator(): Iterator<Pair<CallContext.Key<*>, Any?>> {
        val iterator = synchronized(lock) { map.iterator() }
        return iterator {
            iterator.forEach { (key, value) ->
                yield(key to value)
            }
        }
    }
}
