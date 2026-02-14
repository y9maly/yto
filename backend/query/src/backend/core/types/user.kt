package backend.core.types

import kotlinx.serialization.Serializable as S


sealed interface UserDescriptor {
//    data class BestFriend(val ofUser: UserId) : UserDescriptor
}

sealed interface UserReference : UserDescriptor {
    data class Id(val id: UserId) : UserReference
    data object Random : UserReference
    // RandomSubscriber...
    // RandomFriend...
}

fun UserId.ref() = UserReference.Id(this)

typealias UserFilter = Filter<UserPredicate>
@S sealed interface UserPredicate {
    @S data class Id(val id: UserId) : UserPredicate
    @S data class Ids(val ids: Set<UserId>) : UserPredicate
}
