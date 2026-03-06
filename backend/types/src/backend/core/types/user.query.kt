package backend.core.types

import kotlinx.serialization.Serializable as S


@S sealed interface UserRef {
//    data class BestFriend(val ofUser: UserId) : UserRef
}

@S sealed interface UserLink : UserRef {
    @S data class Id(val id: UserId) : UserLink
    @S data object Random : UserLink
    // RandomSubscriber...
    // RandomFriend...
}

fun UserId.link() = UserLink.Id(this)

typealias UserFilter = Filter<UserPredicate>
@S sealed interface UserPredicate {
    @S data class Id(val id: UserId) : UserPredicate
    @S data class Ids(val ids: Set<UserId>) : UserPredicate
}
