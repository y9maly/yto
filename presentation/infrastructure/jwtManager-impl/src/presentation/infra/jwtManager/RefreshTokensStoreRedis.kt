package presentation.infra.jwtManager

import backend.core.types.SessionId
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands


@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RefreshTokensStoreRedis(
    private val commands: RedisCoroutinesCommands<String, String>,
) : RefreshTokensStore {
    override suspend fun getRefreshTokenJti(forSession: SessionId): String? {
        return commands.get("jwt:refresh-token-${forSession.long}")
    }

    override suspend fun updateRefreshTokenJti(forSession: SessionId, refreshJti: String) {
        commands.set("jwt:refresh-token-${forSession.long}", refreshJti)
    }

    override suspend fun deleteRefreshTokenJti(forSession: SessionId) {
        commands.del("jwt:refresh-token-${forSession.long}")
    }
}
