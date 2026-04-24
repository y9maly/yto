package presentation.infra.updateManager

import backend.core.types.SessionId
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.lettuce.core.Consumer
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.XReadArgs
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.search.arguments.SugAddArgs.Builder.payload
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import y9to.api.types.Update
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalLettuceCoroutinesApi::class)
class UpdateProviderRedisLettuce(
    private val redisCommands: RedisCoroutinesCommands<String, String>,
) : UpdateProvider {
    override suspend fun receive(forSession: SessionId): Update? {
        var update: Update

        while (true) {
            val message = redisCommands.xread(
                XReadArgs.Builder.count(1),
                XReadArgs.StreamOffset.from("update:queue-${forSession.long}", "0"),
            ).firstOrNull() ?: return null

            val payload = message.body["payload"]

            if (payload == null) {
                consume(forSession)
                continue
            }

            update = try {
                Json.decodeFromString(payload)
            } catch (_: SerializationException) {
                consume(forSession)
                continue
            }

            break
        }

        return update
    }

    override suspend fun await(forSession: SessionId): Update {
        while (true) {
            // временно
            delay(50.milliseconds)
            return receive(forSession) ?: continue
        }
    }

    override suspend fun consume(forSession: SessionId) {
        val message = redisCommands.xread(
            XReadArgs.Builder.count(1),
            XReadArgs.StreamOffset.from("update:queue-${forSession.long}", "0-0")
        ).firstOrNull() ?: return

        redisCommands.xdel("update:queue-${forSession.long}", message.id)
    }
}
