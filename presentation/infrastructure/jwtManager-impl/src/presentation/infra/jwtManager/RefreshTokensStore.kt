package presentation.infra.jwtManager

import backend.core.types.SessionId


interface RefreshTokensStore {
    suspend fun getRefreshTokenJti(forSession: SessionId): String?
    suspend fun updateRefreshTokenJti(forSession: SessionId, refreshJti: String)
    suspend fun deleteRefreshTokenJti(forSession: SessionId)
}
