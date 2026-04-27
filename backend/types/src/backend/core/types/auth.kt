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

@S sealed interface LoginState {
    @S data object None : LoginState

    @S data class WaitConfirmCode(val digitOnly: Boolean, val length: Int) : LoginState

    @S data class WaitPassword(val hint: String?) : LoginState

    @S sealed interface WaitRegistration : LoginState {
        @S data object WaitRegistrationDefault : WaitRegistration

        @S data class WaitRegistrationViaTelegram(
            val telegramFirstName: Optional<String>,
            val telegramLastName: Optional<String>,
            val telegramAvatar: Optional<File>,
            val telegramPhoneNumber: Optional<String>,
            var canUseTelegramPhoneNumber: Boolean,
        ) : WaitRegistration {
            init {
                require((telegramPhoneNumber.isNone && !canUseTelegramPhoneNumber) || (telegramPhoneNumber.isPresent)) {
                    "canUseTelegramPhoneNumber must be always false when telegramPhoneNumber is null"
                }
            }
        }
    }

    @S data class TelegramOIDC(
        val authorizationUri: String,
    ) : LoginState
}

@S data class SessionId(val long: Long)

@S data class Session(
    val id: SessionId,
    val revision: Revision,
    val creationDate: Instant,
)
