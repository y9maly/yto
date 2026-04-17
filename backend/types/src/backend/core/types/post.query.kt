package backend.core.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S


@S data class PostId(val long: Long)
@S sealed interface PostRef {
    @SerialName("FirstOfAuthor")
    @S data class FirstOfAuthor(val author: UserId) : PostRef

    @SerialName("RandomOfAuthor")
    @S data class RandomOfAuthor(val author: UserId) : PostRef

    @SerialName("LastOfAuthor")
    @S data class LastOfAuthor(val author: UserId) : PostRef
}

typealias PostPredicate = Predicate<PostCriteria>
@S sealed interface PostCriteria {
    @SerialName("Id")
    @S data class Id(val id: PostId) : PostCriteria

    @SerialName("Location")
    @S data class Location(val location: PostLocationPredicate) : PostCriteria

    @SerialName("ContentType")
    @S data class ContentType(val type: PostContentType) : PostCriteria
}

typealias PostLocationPredicate = Predicate<PostLocationCriteria>
@S sealed interface PostLocationCriteria {
    @SerialName("Global")
    @S data object Global : PostLocationCriteria

    @SerialName("Author")
    @S data class Author(val user: UserPredicate) : PostLocationCriteria

    @SerialName("AnyProfile")
    @S data object AnyProfile : PostLocationCriteria

    @SerialName("Profile")
    @S data class Profile(val user: UserPredicate) : PostLocationCriteria
}
