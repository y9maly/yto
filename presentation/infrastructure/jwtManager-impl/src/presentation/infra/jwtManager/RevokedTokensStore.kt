package presentation.infra.jwtManager

import kotlin.time.Instant


interface RevokedTokensStore {
    suspend fun isRevoked(accessJti: String): Boolean
    suspend fun revoke(accessJti: String, until: Instant)
}
