package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S


@S sealed interface InputAuthMethod {
    @SerialName("PhoneNumber")
    @S data class PhoneNumber(val phoneNumber: String) : InputAuthMethod

    @SerialName("Email")
    @S data class Email(val email: String) : InputAuthMethod
}
