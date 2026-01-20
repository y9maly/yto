package y9to.api.types


sealed interface InputUser {
    data class Id(val id: UserId) : InputUser
//    data class Access(val access: UserAccess) : InputUser
}
