@file:JvmName("InputAuthKt")

package y9to.api.types

import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmName


@S sealed interface InputAuthMethod {
    @S data class PhoneNumber(val phoneNumber: String) : InputAuthMethod
    @S data class Email(val email: String) : InputAuthMethod
}
