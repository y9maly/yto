package presentation.infra.jwtManager


sealed interface RefreshTokensResult {
    data class Ok(val accessToken: String, val refreshToken: String) : RefreshTokensResult
    data object ExpiredRefreshToken : RefreshTokensResult
    data object InvalidRefreshToken : RefreshTokensResult
}
