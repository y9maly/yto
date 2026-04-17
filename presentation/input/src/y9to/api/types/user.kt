@file:JvmName("InputUserKt")

package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmName


@S sealed interface InputUser {
    @SerialName("Id")
    @S data class Id(val id: UserId) : InputUser
}
