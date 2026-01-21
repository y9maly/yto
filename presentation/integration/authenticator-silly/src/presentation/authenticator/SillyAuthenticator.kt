package presentation.authenticator

import backend.core.types.SessionId
import y9to.api.types.Token


class SillyAuthenticator : Authenticator {
    override suspend fun authenticateOrNull(token: Token): SessionId? {
        if (token.unsafe.apiVersion != "0.0.1")
            return null
        return SessionId(token.unsafe.session.long)
    }
}
