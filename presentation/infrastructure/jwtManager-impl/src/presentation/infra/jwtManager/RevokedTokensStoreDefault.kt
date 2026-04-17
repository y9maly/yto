package presentation.infra.jwtManager

import io.github.crackthecodeabhi.kreds.args.SetOption
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import kotlin.time.Instant


class RevokedTokensStoreDefault(
    private val redisClient: KredsClient,
) : RevokedTokensStore {
    override suspend fun isRevoked(accessJti: String): Boolean {
        return redisClient.exists("jwt:revoked-access:$accessJti") == 1L
    }

    override suspend fun revoke(accessJti: String, until: Instant) {
        redisClient.set("jwt:revoked-access:$accessJti", "revoked", SetOption.Builder()
            .exatTimestamp(until.toEpochMilliseconds().toULong())
            .build())
    }
}
