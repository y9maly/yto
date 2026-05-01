package domain.event

import backend.core.types.Post
import backend.core.types.PostContent
import backend.core.types.PostId
import backend.core.types.UserId
import y9to.libs.stdlib.optional.Optional
import kotlinx.serialization.Serializable as S


@S data class PostCreated(val post: Post) : Event

@S data class PostEdited(
    val postId: PostId,
    val newAuthor: Optional<UserId>,
    val newReplyTo: Optional<PostId?>,
    val newContent: Optional<PostContent>,
) : Event

@S data class PostDeleted(val postId: PostId) : Event
