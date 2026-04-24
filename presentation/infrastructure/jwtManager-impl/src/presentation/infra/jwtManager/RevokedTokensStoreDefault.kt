package presentation.infra.jwtManager

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlin.time.Instant
import kotlin.time.toJavaInstant


@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RevokedTokensStoreDefault(
    private val commands: RedisCoroutinesCommands<String, String>,
) : RevokedTokensStore {
    override suspend fun isRevoked(accessJti: String): Boolean {
        return commands.exists("jwt:revoked-access:$accessJti") != 0L
    }

    override suspend fun revoke(accessJti: String, until: Instant) {
        commands.set("jwt:revoked-access:$accessJti", "revoked", SetArgs.Builder
            .exAt(until.toJavaInstant()))
    }
}
