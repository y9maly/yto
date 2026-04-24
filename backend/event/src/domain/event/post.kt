package domain.event

import backend.core.types.Post
import backend.core.types.PostContent
import backend.core.types.PostId
import kotlinx.serialization.Serializable as S


@S data class PostCreated(val post: Post) : Event

@S data class PostContentEdited(val postId: PostId, val oldContent: PostContent, val newContent: PostContent) : Event

@S data class PostDeleted(val postId: PostId) : Event
