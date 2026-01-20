package backend.core.types

import kotlin.time.Instant


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
    val revision: Revision,
    val creationDate: Instant,
)
