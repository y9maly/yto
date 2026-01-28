package backend.core.types

import kotlin.time.Instant


@JvmInline
value class PostId(val long: Long)


data class Post(
    val id: PostId,
    val revision: Revision,
    val replyTo: PostReplyHeader?,
    val author: UserPreview,
    val publishDate: Instant,
    val lastEditDate: Instant?,
    val content: PostContent,
)

sealed interface PostReplyHeader {
    val publishDate: Instant
    val author: UserPreview

    data class Post(
        val postId: PostId,
        override val publishDate: Instant,
        override val author: UserPreview,
    ) : PostReplyHeader

    data class DeletedPost(
        override val publishDate: Instant,
        override val author: UserPreview,
    ) : PostReplyHeader
}

sealed interface PostAuthorPreview {
    data class User(val id: UserId, val firstName: String, val lastName: String?) : PostAuthorPreview
    data class DeletedUser(val firstName: String, val lastName: String?) : PostAuthorPreview
}

sealed interface RepostPreview {
    val author: PostAuthorPreview
    val publishDate: Instant
    val lastEditDate: Instant?

    data class Post(
        val postId: PostId,
        override val author: PostAuthorPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
        val content: PostContent,
    ) : RepostPreview

    data class DeletedPost(
        val deletionDate: Instant,
        override val author: PostAuthorPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
    ) : RepostPreview
}

enum class PostContentType { Standalone, Repost }

sealed interface PostContent {
    data class Standalone(val text: String) : PostContent

    data class Repost(
        val preview: RepostPreview,
        val comment: String?,
    ) : PostContent
}
