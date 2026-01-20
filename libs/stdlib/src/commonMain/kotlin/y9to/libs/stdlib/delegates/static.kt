package y9to.libs.stdlib.delegates

import kotlin.properties.ReadOnlyProperty


/**
 * Чтобы делать static val extensions;
 *
 * ```kotlin
 * import io.ktor.http.HttpStatusCode
 *
 * val HttpStatusCode.Companion.Answer by static {
 *     HttpStatusCode(42, "Answer")
 * }
 * ```
 *
 * by lazy даёт тот же результат, но с lazy инициализацией.
 */
inline fun <T> static(getValue: () -> T): ReadOnlyProperty<Any?, T> {
    val value = getValue()
    return ReadOnlyProperty { _, _ -> value }
}

@Suppress("UNCHECKED_CAST")
fun <R, T> memoized(getValue: R.() -> T): ReadOnlyProperty<R, T> {
    var cached: T? = null
    var hasCached = false
    return ReadOnlyProperty { receiver, _ ->
        if (hasCached)
            return@ReadOnlyProperty cached as T
        val value = getValue(receiver)
        cached = value
        hasCached = true
        value
    }
}
