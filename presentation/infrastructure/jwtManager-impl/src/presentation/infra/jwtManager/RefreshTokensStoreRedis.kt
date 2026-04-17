package presentation.infra.jwtManager

import backend.core.types.SessionId
import io.github.crackthecodeabhi.kreds.connection.KredsClient


class RefreshTokensStoreRedis(
    private val redisClient: KredsClient,
) : RefreshTokensStore {
    override suspend fun getRefreshTokenJti(forSession: SessionId): String? {
        return redisClient.get("jwt:refresh-token-${forSession.long}")
    }

    override suspend fun updateRefreshTokenJti(forSession: SessionId, refreshJti: String) {
        redisClient.set("jwt:refresh-token-${forSession.long}", refreshJti)
    }

    override suspend fun deleteRefreshTokenJti(forSession: SessionId) {
        redisClient.del("jwt:refresh-token-${forSession.long}")
    }
}
