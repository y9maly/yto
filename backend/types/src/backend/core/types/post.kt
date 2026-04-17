package backend.core.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import kotlin.time.Instant


@S data class Post(
    val id: PostId,
    val revision: Revision,
    val location: PostLocation,
    val replyTo: PostReplyHeader?,
    val author: UserPreview,
    val publishDate: Instant,
    val lastEditDate: Instant?,
    val content: PostContent,
)

@S sealed interface PostLocation {
    @SerialName("Global")
    @S data object Global : PostLocation

    @SerialName("Profile")
    @S data class Profile(val user: UserId) : PostLocation
}

@S sealed interface PostReplyHeader {
    val publishDate: Instant
    val author: UserPreview

    @SerialName("Post")
    @S data class Post(
        val postId: PostId,
        override val publishDate: Instant,
        override val author: UserPreview,
    ) : PostReplyHeader

    @SerialName("DeletedPost")
    @S data class DeletedPost(
        override val publishDate: Instant,
        override val author: UserPreview,
    ) : PostReplyHeader
}

@S sealed interface PostAuthorPreview {
    @SerialName("User")
    @S data class User(val id: UserId, val firstName: String, val lastName: String?) : PostAuthorPreview

    @SerialName("DeletedUser")
    @S data class DeletedUser(val firstName: String, val lastName: String?) : PostAuthorPreview
}

@S sealed interface RepostPreview {
    val author: PostAuthorPreview
    val publishDate: Instant
    val lastEditDate: Instant?

    @SerialName("Post")
    @S data class Post(
        val postId: PostId,
        override val author: PostAuthorPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
        val content: PostContent,
    ) : RepostPreview

    @SerialName("DeletedPost")
    @S data class DeletedPost(
        val deletionDate: Instant,
        override val author: PostAuthorPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
    ) : RepostPreview
}

@S enum class PostContentType { Standalone, Repost }

@S sealed interface PostContent {
    @SerialName("Standalone")
    @S data class Standalone(val text: String) : PostContent

    @SerialName("Repost")
    @S data class Repost(
        val preview: RepostPreview,
        val comment: String?,
    ) : PostContent
}
