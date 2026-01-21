package backend.core.reference

import backend.core.types.UserId


sealed interface UserReference {
    data class Id(val id: UserId) : UserReference
    data object Random : UserReference
    // RandomSubscriber...
    // RandomFriend...
}

fun UserId.ref() = UserReference.Id(this)
