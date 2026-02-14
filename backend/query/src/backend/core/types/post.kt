package backend.core.types

import kotlinx.serialization.Serializable as S


sealed interface PostDescriptor {
//    data class FirstOfAuthor(val author: UserDescriptor) : PostDescriptor
//    data class RandomOfAuthor(val author: UserDescriptor) : PostDescriptor
//    data class LastOfAuthor(val author: UserDescriptor) : PostDescriptor
}

sealed interface PostReference : PostDescriptor {
    data class Id(val id: PostId) : PostReference
    data class RandomOf(val filter: PostFilter) : PostReference
    data class FirstOfAuthor(val author: UserReference) : PostReference
    data class RandomOfAuthor(val author: UserReference) : PostReference
    data class LastOfAuthor(val author: UserReference) : PostReference
    data object First : PostReference
    data object Random : PostReference
    data object Last : PostReference
}

typealias PostFilter = Filter<PostPredicate>
@S sealed interface PostPredicate {
    @S data class Id(val id: PostId) : PostPredicate
    @S data class Location(val location: PostLocationFilter) : PostPredicate
    @S data class Content(val type: PostContentType) : PostPredicate
}

typealias PostLocationFilter = Filter<PostLocationPredicate>
@S sealed interface PostLocationPredicate {
    @S data object Global : PostLocationPredicate
    @S data class Profile(val user: UserFilter) : PostLocationPredicate
}
