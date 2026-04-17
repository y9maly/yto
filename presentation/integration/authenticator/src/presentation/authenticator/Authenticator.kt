package presentation.authenticator

import y9to.api.types.Token


interface Authenticator {
    suspend fun authenticate(token: Token): AuthenticateResult
}

suspend fun Authenticator.authenticateOrThrow(token: Token): AuthenticateResult.Ok = when (
    val result = authenticate(token)
) {
    is AuthenticateResult.Ok -> result
    is AuthenticateResult.InvalidToken -> error("Invalid token")
    is AuthenticateResult.RevokedToken -> error("Revoked token")
    is AuthenticateResult.ExpiredToken -> error("Expired token")
}
