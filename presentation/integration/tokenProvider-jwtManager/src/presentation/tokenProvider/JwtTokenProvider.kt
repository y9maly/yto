package presentation.tokenProvider

import backend.core.types.SessionId
import presentation.infra.jwtManager.IssueTokensResult
import presentation.infra.jwtManager.JwtManager
import presentation.infra.jwtManager.RefreshTokenPayload
import y9to.api.types.RefreshToken
import y9to.api.types.Token
import presentation.infra.jwtManager.RefreshTokensResult as JwtRefreshAccessTokenResult


class JwtTokenProvider(
    private val jwtManager: JwtManager,
) : TokenProvider {
    override suspend fun issueTokens(forSession: SessionId): Pair<RefreshToken, Token>? {
        return when (val result = jwtManager.issueTokens(forSession)) {
            is IssueTokensResult.Ok -> RefreshToken(result.refreshToken) to Token(result.accessToken)
            IssueTokensResult.InvalidSessionId -> null
        }
    }

    override suspend fun refreshTokens(refreshToken: RefreshToken): RefreshTokensResult {
        return when (val result = jwtManager.refreshTokens(refreshToken.string)) {
            is JwtRefreshAccessTokenResult.Ok -> RefreshTokensResult.Ok(
                refreshToken = RefreshToken(result.refreshToken),
                accessToken = Token(result.accessToken),
            )
            JwtRefreshAccessTokenResult.ExpiredRefreshToken -> RefreshTokensResult.ExpiredRefreshToken
            JwtRefreshAccessTokenResult.InvalidRefreshToken -> RefreshTokensResult.InvalidRefreshToken
        }
    }
}
