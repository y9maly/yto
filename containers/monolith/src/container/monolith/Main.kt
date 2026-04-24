@file:Suppress("SameParameterValue")

package container.monolith

import container.monolith.StartKtorServerConfig.Cors
import container.monolith.StartKtorServerConfig.StaticFiles
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlin.system.exitProcess


private const val corsHostStrategyHelp = """Must be a JSON array. For example: '["http://domain.zone", "https://subdomain.domain.zone"]' OR '*'. Use '*' to allow any host."""
private const val staticFilesHelp = """Must be a JSON array. For example: '[{"remote_path": "/", "directory": "/files/staticContent", "default": "index.html"}]'. "default" key is optional."""

@Serializable
@OptIn(ExperimentalSerializationApi::class)
private class StaticFilesElement(
    @JsonNames("remote_path")
    val remotePath: String,
    val directory: String,
    val default: String? = null,
)

suspend fun main() {
    val corsHostStrategyRaw = Env.require("CORS_HOST_STRATEGY", corsHostStrategyHelp)
    val staticFilesRaw = Env.orDefault("STATIC_FILES", "[]")

    val monolith = instantiate()

    GlobalScope.launch {
        try {
            monolith.startWorkers()
        } catch (t: Throwable) {
            t.printStackTrace()
            exitProcess(-1)
        }
    }

    monolith.startKtorServer(
        config = StartKtorServerConfig(
            host = MonolithDefaults.host,
            port = MonolithDefaults.port,
            cors = Cors(
                hosts = if (corsHostStrategyRaw == "*") {
                    null // allow any host
                } else {
                    runCatching { Json.decodeFromString<List<String>>(corsHostStrategyRaw) }
                        .getOrElse { error("Invalid CORS_HOST_STRATEGY environment variable. $corsHostStrategyHelp") }
                }
            ),
            staticFiles = runCatching { Json.decodeFromString<List<StaticFilesElement>>(staticFilesRaw) }
                .getOrElse { error("Invalid STATIC_FILES environment variable. $staticFilesHelp") }
                .map { element ->
                    StaticFiles(
                        remotePath = element.remotePath,
                        directory = element.directory,
                        default = element.default,
                    )
                }
        ),
        wait = true
    )
}

private object Env {
    fun require(name: String, help: String = ""): String {
        return System.getenv(name) ?: error("Environment variable '$name' is required. $help")
    }

    fun orDefault(name: String, default: String): String {
        return System.getenv(name) ?: default
    }
}
