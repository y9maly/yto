@file:JvmName("TypePostKt")

package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.time.Instant


@SerialName("PostId")
@S data class PostId(val long: Long) : InputPost


@S data class Post(
    val id: PostId,
    val replyTo: PostReplyHeader?,
    val author: UserPreview,
    val publishDate: Instant,
    val lastEditDate: Instant?,
    val content: PostContent,
)

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
    val firstName: String
    val lastName: String?

    @SerialName("User")
    @S data class User(val id: UserId, override val firstName: String, override val lastName: String?) : PostAuthorPreview

    @SerialName("DeletedUser")
    @S data class DeletedUser(override val firstName: String, override val lastName: String?) : PostAuthorPreview
}

@S sealed interface RepostPreview{
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
