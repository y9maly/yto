package y9to.api.types


import kotlinx.serialization.Serializable as S


@S sealed interface InputUser {
    @S data object Me : InputUser
}
