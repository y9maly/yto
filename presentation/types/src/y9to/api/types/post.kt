package y9to.api.types

import kotlin.jvm.JvmInline
import kotlin.time.Instant


// todo -> PostAccessHash/Ref
@JvmInline
value class PostId(val long: Long)


data class Post(
    val id: PostId,
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

sealed interface RepostHeader {
    val author: UserPreview
    val publishDate: Instant
    val lastEditDate: Instant?

    data class Post(
        val postId: PostId,
        override val author: UserPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
    ) : RepostHeader

    data class DeletedPost(
        val deletionDate: Instant,
        override val author: UserPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
    ) : RepostHeader
}

enum class PostContentType { Standalone, Repost }

sealed interface PostContent {
    data class Standalone(val text: String) : PostContent

    data class Repost(
        val header: RepostHeader,
        val comment: String?,
    ) : PostContent
}
