package backend.core.reference

import backend.core.types.UserId


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
