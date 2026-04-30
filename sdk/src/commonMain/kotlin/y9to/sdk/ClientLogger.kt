package y9to.sdk

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.github.oshai.kotlinlogging.Level
import io.github.oshai.kotlinlogging.Marker


interface ClientLogger {
    val name: String get() = "yto.sdk.Client"

    fun isLoggingEnabledFor(componentName: String, level: Level, marker: Marker? = null): Boolean

    fun at(componentName: String, level: Level, marker: Marker? = null, block: KLoggingEventBuilder.() -> Unit)
}

object EmptyClientLogger : ClientLogger {
    override fun isLoggingEnabledFor(componentName: String, level: Level, marker: Marker?): Boolean {
        return false
    }

    override fun at(componentName: String, level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {

    }
}

fun ClientLogger(
    kLogger: KLogger,
    message: (componentName: String, message: String) -> String = { componentName, message -> "[$componentName] $message" },
) = object : ClientLogger {
    override val name = kLogger.name

    override fun isLoggingEnabledFor(componentName: String, level: Level, marker: Marker?): Boolean {
        return kLogger.isLoggingEnabledFor(level, marker)
    }

    override fun at(componentName: String, level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
        return kLogger.at(level, marker) {
            block()
            this.message = message(componentName, this.message ?: return@at)
        }
    }
}
