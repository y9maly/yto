package presentation.infra.updateManager

import backend.core.types.SessionId
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.serialization.json.Json
import y9to.api.types.Update


@OptIn(ExperimentalLettuceCoroutinesApi::class)
class UpdateProducerRedisKreds(
    private val client: KredsClient,
)  : UpdateProducer {
    override suspend fun emit(forSession: SessionId, update: Update) {
        val payload = Json.encodeToString(update)
        client.rpush("update:queue-${forSession.long}", payload)
    }
}

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class UpdateProducerRedisLettuce(
    private val redisCommands: RedisCoroutinesCommands<String, String>,
)  : UpdateProducer {
    override suspend fun emit(forSession: SessionId, update: Update) {
        val payload = Json.encodeToString(update)
        redisCommands.xadd("update:queue-${forSession.long}", mapOf("payload" to payload))
    }
}
