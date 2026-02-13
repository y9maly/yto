package integration.repository.input

import backend.core.types.Filter
import backend.core.types.PostContentType
import backend.core.types.PostId
import backend.core.types.UserId
import integration.repository.input.InputPostContent.Repost
import integration.repository.input.InputPostContent.Standalone
import kotlinx.serialization.Serializable


sealed interface InputPostLocation {
    data object Global : InputPostLocation
    data class Profile(val user: UserId) : InputPostLocation
}

typealias PostFilter = Filter<PostPredicate>
@Serializable
sealed interface PostPredicate {
    @Serializable
    data class Id(val id: PostId) : PostPredicate
    @Serializable
    data class Location(val location: PostLocationFilter) : PostPredicate
    @Serializable
    data class Content(val type: PostContentType) : PostPredicate
}

typealias PostLocationFilter = Filter<PostLocationPredicate>
@Serializable
sealed interface PostLocationPredicate {
    @Serializable
    data object Global : PostLocationPredicate
    @Serializable
    data class Profile(val user: UserFilter) : PostLocationPredicate
}

sealed interface InputPostContent {
    data class Standalone(val text: String) : InputPostContent
    data class Repost(val comment: String?, val original: PostId) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is Standalone -> PostContentType.Standalone
    is Repost -> PostContentType.Repost
}
