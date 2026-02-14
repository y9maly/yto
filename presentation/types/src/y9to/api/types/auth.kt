@file:JvmName("TypeAuthKt")

package y9to.api.types

import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.time.Instant


@S data class Token(
    val unsafe: Unsafe
) {
    @S data class Unsafe(
        val session: SessionId,
        val apiVersion: String,
    )
}


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
    val creationDate: Instant,
)
