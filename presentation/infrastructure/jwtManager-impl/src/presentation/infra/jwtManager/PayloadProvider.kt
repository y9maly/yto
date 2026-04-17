package presentation.infra.jwtManager

import backend.core.types.SessionId


interface PayloadProvider {
    /**
     * @return null if invalid [forSession] session id
     */
    suspend fun getAccessTokenPayload(forSession: SessionId): AccessTokenPayload?
}
