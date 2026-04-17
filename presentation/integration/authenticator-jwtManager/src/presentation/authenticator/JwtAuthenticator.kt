package presentation.authenticator

import presentation.infra.jwtManager.AuthenticateResult as JwtAuthenticateResult
import presentation.infra.jwtManager.JwtManager
import y9to.api.types.Token
import y9to.libs.stdlib.optional.present
import kotlin.uuid.ExperimentalUuidApi


class JwtAuthenticator(
    private val jwtManager: JwtManager,
) : Authenticator {
    override suspend fun authenticate(token: Token): AuthenticateResult {
        return when (val result = jwtManager.authenticate(token.string)) {
            is JwtAuthenticateResult.Ok -> AuthenticateResult.Ok(
                sessionId = result.payload.sessionId,
                authState = present(result.payload.authState),
            )

            JwtAuthenticateResult.ExpiredAccessToken -> AuthenticateResult.ExpiredToken
            JwtAuthenticateResult.InvalidAccessToken -> AuthenticateResult.InvalidToken
            JwtAuthenticateResult.RevokedAccessToken -> AuthenticateResult.RevokedToken
        }
    }
}
