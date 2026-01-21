package presentation.authenticator

import backend.core.types.SessionId
import y9to.api.types.Token


interface Authenticator {
    suspend fun authenticate(token: Token) = authenticateOrNull(token)
        ?: error("Invalid token")

    suspend fun authenticateOrNull(token: Token): SessionId?
}
