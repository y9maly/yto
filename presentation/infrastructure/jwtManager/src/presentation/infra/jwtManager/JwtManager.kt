package presentation.infra.jwtManager

import backend.core.types.SessionId


interface JwtManager {
    suspend fun authenticate(accessToken: String): AuthenticateResult

    suspend fun issueTokens(forSession: SessionId): IssueTokensResult
    suspend fun refreshTokens(refreshToken: String): RefreshTokensResult

    suspend fun revokeAccessToken(accessToken: String)
    suspend fun revokeRefreshToken(forSession: SessionId)
}
