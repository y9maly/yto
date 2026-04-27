package integration.loginRepository

import backend.core.types.SessionId
import domain.service.InternalLoginState
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


@OptIn(ExperimentalLettuceCoroutinesApi::class)
class LoginRepositoryRedis(
    private val commands: RedisCoroutinesCommands<String, String>,
) : LoginRepository {
    override suspend fun saveLoginState(session: SessionId, state: InternalLoginState, ttl: Duration) {
        commands.set(
            "login:internalState-${session.long}",
            Json.encodeToString(state),
            SetArgs.Builder.ex(ttl.coerceAtLeast(1.seconds).toJavaDuration())
        )
    }

    override suspend fun getLoginState(session: SessionId): InternalLoginState? {
        val string = commands.get("login:internalState-${session.long}")
            ?: return null
        return runCatching { Json.decodeFromString<InternalLoginState>(string) }
            .getOrElse {
                resetLoginState(session)
                null
            }
    }

    override suspend fun resetLoginState(session: SessionId) {
        commands.del("login:internalState-${session.long}")
    }
}
