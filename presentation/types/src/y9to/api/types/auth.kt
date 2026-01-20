package y9to.api.types

import kotlin.jvm.JvmInline
import kotlin.time.Instant


data class Token(
    val unsafe: Unsafe
) {
    data class Unsafe(
        val session: SessionId,
        val apiVersion: String,
    )
}


/**
 * User or Chatbot
 */
sealed interface AuthorizableId

sealed interface AuthState {
    data object Unauthorized : AuthState
    data class Authorized(val id: AuthorizableId) : AuthState

    fun idOrNull() = (this as? Authorized)?.id
}


@JvmInline
value class SessionId(val long: Long)

data class Session(
    val id: SessionId,
    val creationDate: Instant,
)
