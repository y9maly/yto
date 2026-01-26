package y9to.api.types

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant


// todo -> PostAccessHash/Ref
@Serializable
@JvmInline
value class PostId(val long: Long)


@Serializable
data class Post(
    val id: PostId,
    val replyTo: PostReplyHeader?,
    val author: UserPreview,
    val publishDate: Instant,
    val lastEditDate: Instant?,
    val content: PostContent,
)

@Serializable
sealed interface PostReplyHeader {
    val publishDate: Instant
    val author: UserPreview

    @Serializable
    data class Post(
        val postId: PostId,
        override val publishDate: Instant,
        override val author: UserPreview,
    ) : PostReplyHeader

    @Serializable
    data class DeletedPost(
        override val publishDate: Instant,
        override val author: UserPreview,
    ) : PostReplyHeader
}

@Serializable
sealed interface RepostPreview{
    val author: UserPreview
    val publishDate: Instant
    val lastEditDate: Instant?

    @Serializable
    data class Post(
        val postId: PostId,
        override val author: UserPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
        val content: PostContent,
    ) : RepostPreview

    @Serializable
    data class DeletedPost(
        val deletionDate: Instant,
        override val author: UserPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
    ) : RepostPreview
}

@Serializable
enum class PostContentType { Standalone, Repost }

@Serializable
sealed interface PostContent {
    @Serializable
    data class Standalone(val text: String) : PostContent

    @Serializable
    data class Repost(
        val preview: RepostPreview,
        val comment: String?,
    ) : PostContent
}
