package y9to.api.types

import kotlinx.serialization.Serializable


@Serializable
sealed interface InputUser {
    @Serializable
    data class Id(val id: UserId) : InputUser
//    data class Access(val access: UserAccess) : InputUser
}
