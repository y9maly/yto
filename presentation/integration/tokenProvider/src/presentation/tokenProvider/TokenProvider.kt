package presentation.tokenProvider

import backend.core.types.SessionId
import y9to.api.types.RefreshToken
import y9to.api.types.Token


interface TokenProvider {
    suspend fun issueTokens(forSession: SessionId): Pair<RefreshToken, Token>?
    suspend fun refreshTokens(refreshToken: RefreshToken): RefreshTokensResult
}
