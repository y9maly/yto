package backend.core.types

import kotlinx.serialization.SerialName
import y9to.libs.stdlib.optional.Optional
import kotlin.time.Instant
import kotlinx.serialization.Serializable as S


/**
 * User or Chatbot
 */
@S sealed interface ClientId

@S sealed interface AuthState {
    @SerialName("Unauthorized")
    @S data object Unauthorized : AuthState

    @SerialName("Authorized")
    @S data class Authorized(val id: ClientId) : AuthState

    fun idOrNull() = (this as? Authorized)?.id
    fun userIdOrNull() = (this as? Authorized)?.id as? UserId?
}

@S data class SessionId(val long: Long)

@S data class Session(
    val id: SessionId,
    val revision: Revision,
    val creationDate: Instant,
)
