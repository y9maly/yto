@file:JvmName("TypeAuthKt")

package y9to.api.types

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.time.Instant


@Serializable
data class Token(
    val unsafe: Unsafe
) {
    @Serializable
    data class Unsafe(
        val session: SessionId,
        val apiVersion: String,
    )
}


/**
 * User or Chatbot
 */
@Serializable
sealed interface ClientId

@Serializable
sealed interface AuthState {
    @Serializable
    data object Unauthorized : AuthState
    @Serializable
    data class Authorized(val id: ClientId) : AuthState

    fun idOrNull() = (this as? Authorized)?.id
    fun userIdOrNull() = (this as? Authorized)?.id as? UserId?
}


@Serializable
@JvmInline
value class SessionId(val long: Long)

@Serializable
data class Session(
    val id: SessionId,
    val creationDate: Instant,
)
