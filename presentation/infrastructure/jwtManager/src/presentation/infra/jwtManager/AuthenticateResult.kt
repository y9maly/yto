package presentation.infra.jwtManager


sealed interface AuthenticateResult {
    data class Ok(val payload: AccessTokenPayload) : AuthenticateResult
    data object ExpiredAccessToken : AuthenticateResult
    data object RevokedAccessToken : AuthenticateResult
    data object InvalidAccessToken : AuthenticateResult
}
