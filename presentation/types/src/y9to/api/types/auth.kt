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

@S sealed interface LoginState {
    @SerialName("None")
    @S data object None : LoginState

    @SerialName("WaitConfirmCode")
    @S data class WaitConfirmCode(val digitOnly: Boolean, val length: Int) : LoginState

    @SerialName("WaitPassword")
    @S data class WaitPassword(val hint: String?) : LoginState

    @SerialName("WaitRegistration")
    @S sealed interface WaitRegistration : LoginState {
        @SerialName("WaitRegistrationDefault")
        @S data object WaitRegistrationDefault : WaitRegistration

        /**
         * User is authenticating via telegram.
         * [telegramFirstName], [telegramLastName] - UI can this values as default "first name" and "last name" values in text field.
         * [telegramAvatar] - Avatar loaded from telegram profile. UI can suggest to use it while registration.
         * [telegramPhoneNumber] - Phone number from telegram.
         * [canUseTelegramPhoneNumber] - If true then [telegramPhoneNumber] is not null and UI must ask user to use it as profile phone number or not.
         */
        @SerialName("WaitRegistrationViaTelegram")
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

    @SerialName("TelegramOIDC")
    @S data class TelegramOIDC(
        val authorizationUri: String,
    ) : LoginState
}

@JvmInline
@S value class SessionId(val long: Long)

@S data class Session(
    val id: SessionId,
    val creationDate: Instant,
)
