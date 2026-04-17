package backend.core.types

import kotlinx.serialization.SerialName
import y9to.libs.stdlib.serialization.RegexSerializer
import kotlinx.serialization.Serializable as S


@SerialName("UserId")
@S data class UserId(val long: Long) : UserRef, ClientId

@S sealed interface UserRef {

}

typealias UserPredicate = Predicate<UserCriteria>
@S sealed interface UserCriteria {
    @SerialName("Id")
    @S data class Id(val id: UserId) : UserCriteria

    @SerialName("FirstName")
    @S sealed interface FirstName : UserCriteria {
        @SerialName("FirstNameMatches")
        @S data class Matches(val regex: @S(RegexSerializer::class) Regex) : FirstName
    }

    @SerialName("LastName")
    @S sealed interface LastName : UserCriteria {
        @SerialName("LastNameNull")
        @S data object Null : LastName

        @SerialName("LastNameNotNull")
        @S data object NotNull : LastName

        @SerialName("LastNameMatches")
        @S data class Matches(val regex: @S(RegexSerializer::class) Regex) : LastName
    }
}
