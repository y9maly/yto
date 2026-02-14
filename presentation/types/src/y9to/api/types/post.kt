@file:JvmName("TypePostKt")

package y9to.api.types

import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.time.Instant


// todo -> PostAccessHash/Ref
@JvmInline
@S value class PostId(val long: Long)


@S data class Post(
    val id: PostId,
    val replyTo: PostReplyHeader?,
    val author: UserPreview,
    val publishDate: Instant,
    val lastEditDate: Instant?,
    val content: PostContent,
    val canEdit: Boolean,
    val canDelete: Boolean,
)

@S sealed interface PostReplyHeader {
    val publishDate: Instant
    val author: UserPreview

    @S data class Post(
        val postId: PostId,
        override val publishDate: Instant,
        override val author: UserPreview,
    ) : PostReplyHeader

    @S data class DeletedPost(
        override val publishDate: Instant,
        override val author: UserPreview,
    ) : PostReplyHeader
}

@S sealed interface PostAuthorPreview {
    val firstName: String
    val lastName: String?

    @S data class User(val id: UserId, override val firstName: String, override val lastName: String?) : PostAuthorPreview

    @S data class DeletedUser(override val firstName: String, override val lastName: String?) : PostAuthorPreview
}

@S sealed interface RepostPreview{
    val author: PostAuthorPreview
    val publishDate: Instant
    val lastEditDate: Instant?

    @S data class Post(
        val postId: PostId,
        override val author: PostAuthorPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
        val content: PostContent,
    ) : RepostPreview

    @S data class DeletedPost(
        val deletionDate: Instant,
        override val author: PostAuthorPreview,
        override val publishDate: Instant,
        override val lastEditDate: Instant?,
    ) : RepostPreview
}

@S enum class PostContentType { Standalone, Repost }

@S sealed interface PostContent {
    @S data class Standalone(val text: String) : PostContent

    @S data class Repost(
        val preview: RepostPreview,
        val comment: String?,
    ) : PostContent
}
