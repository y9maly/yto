@file:JvmName("InputUserKt")

package y9to.api.types

import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmName


@S sealed interface InputUser {
    @S data class Id(val id: UserId) : InputUser
//    data class Access(val access: UserAccess) : InputUser
}
