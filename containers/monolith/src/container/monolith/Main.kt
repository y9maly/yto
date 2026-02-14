@file:Suppress("SameParameterValue")

package container.monolith

import y9to.api.types.SessionId
import y9to.api.types.Token


suspend fun main() {
    val monolith = instantiate()

    monolith.startKtorServer(
        host = MonolithDefaults.host,
        port = MonolithDefaults.port,
        wait = true
    )
}

fun Token(sessionId: Long) = Token(Token.Unsafe(SessionId(sessionId), "0.0.1"))
