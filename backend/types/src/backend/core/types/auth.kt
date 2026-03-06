package backend.core.types

import kotlin.time.Instant
import kotlinx.serialization.Serializable as S


/**
 * User or Chatbot
 */
@S sealed interface ClientId

@S sealed interface AuthState {
    @S data object Unauthorized : AuthState
    @S data class Authorized(val id: ClientId) : AuthState

    fun idOrNull() = (this as? Authorized)?.id
    fun userIdOrNull() = (this as? Authorized)?.id as? UserId?
}


@JvmInline
@S value class SessionId(val long: Long)

@S data class Session(
    val id: SessionId,
    val revision: Revision,
    val creationDate: Instant,
)
