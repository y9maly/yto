package backend.core.types

import kotlin.time.Instant


/**
 * User or Chatbot
 */
sealed interface ClientId

sealed interface AuthState {
    data object Unauthorized : AuthState
    data class Authorized(val id: ClientId) : AuthState

    fun idOrNull() = (this as? Authorized)?.id
    fun userIdOrNull() = (this as? Authorized)?.id as? UserId?
}


@JvmInline
value class SessionId(val long: Long)

data class Session(
    val id: SessionId,
    val revision: Revision,
    val creationDate: Instant,
)
