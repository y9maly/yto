package backend.core.types

import kotlinx.serialization.Serializable as S


@JvmInline
@S value class PostId(val long: Long)

@S sealed interface PostRef {
//    data class FirstOfAuthor(val author: UserRef) : PostRef
//    data class RandomOfAuthor(val author: UserRef) : PostRef
//    data class LastOfAuthor(val author: UserRef) : PostRef
}

@S sealed interface PostLink : PostRef {
    @S data class Id(val id: PostId) : PostLink
    @S data class RandomOf(val filter: PostFilter) : PostLink
    @S data class FirstOfAuthor(val author: UserLink) : PostLink
    @S data class RandomOfAuthor(val author: UserLink) : PostLink
    @S data class LastOfAuthor(val author: UserLink) : PostLink
    @S data object First : PostLink
    @S data object Random : PostLink
    @S data object Last : PostLink
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
