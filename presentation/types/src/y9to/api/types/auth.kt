@file:JvmName("TypeAuthKt")

package y9to.api.types

import kotlinx.serialization.SerialName
import y9to.libs.stdlib.optional.Optional
import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.time.Instant


// Refresh token
@S data class RefreshToken(val string: String)

// Access token
@S data class Token(val string: String)


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

@JvmInline
@S value class SessionId(val long: Long)

@S data class Session(
    val id: SessionId,
    val creationDate: Instant,
)
