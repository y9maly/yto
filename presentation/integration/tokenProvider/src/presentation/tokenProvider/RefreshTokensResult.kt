package presentation.tokenProvider

import y9to.api.types.RefreshToken
import y9to.api.types.Token


sealed interface RefreshTokensResult {
    data class Ok(val refreshToken: RefreshToken, val accessToken: Token) : RefreshTokensResult
    data object ExpiredRefreshToken : RefreshTokensResult
    data object InvalidRefreshToken : RefreshTokensResult
}
