package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S


@S sealed interface InputAuthMethod {
    @SerialName("PhoneNumber")
    @S data class PhoneNumber(val phoneNumber: String) : InputAuthMethod

    @SerialName("Email")
    @S data class Email(val email: String) : InputAuthMethod

    /**
     * [requestPhoneNumber] - need to request phone number from telegram.
     * If false phone number will no requested.
     * If true phone number will request and suggested to attach as profile phone number.
     */
    @SerialName("Telegram")
    @S data class Telegram(
        val requestPhoneNumber: Boolean,
    ) : InputAuthMethod
}
