package integration.repository.types

import backend.core.types.PostId
import backend.core.types.UserPreview
import kotlin.time.Instant


data class DeletedPost(
    val id: PostId,
    val author: UserPreview,
    val publishDate: Instant,
    val deletionDate: Instant,
    val lastEditDate: Instant?,
)
