@file:JvmName("InputAuthKt")

package y9to.api.types

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName


@Serializable
sealed interface InputAuthMethod {
    @Serializable
    data class PhoneNumber(val phoneNumber: String) : InputAuthMethod
    @Serializable
    data class Email(val email: String) : InputAuthMethod
}
