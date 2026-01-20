package y9to.api.types


sealed interface InputAuthMethod {
    data class PhoneNumber(val phoneNumber: String) : InputAuthMethod
    data class Email(val email: String) : InputAuthMethod
}
