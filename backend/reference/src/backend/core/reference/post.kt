package backend.core.reference

import backend.core.types.PostId


sealed interface PostDescriptor {
//    data class FirstOfAuthor(val author: UserDescriptor) : PostDescriptor
//    data class RandomOfAuthor(val author: UserDescriptor) : PostDescriptor
//    data class LastOfAuthor(val author: UserDescriptor) : PostDescriptor
}

sealed interface PostReference : PostDescriptor {
    data class Id(val id: PostId) : PostReference
    data class FirstOfAuthor(val author: UserReference) : PostReference
    data class RandomOfAuthor(val author: UserReference) : PostReference
    data class LastOfAuthor(val author: UserReference) : PostReference
    data object First : PostReference
    data object Random : PostReference
    data object Last : PostReference
}
