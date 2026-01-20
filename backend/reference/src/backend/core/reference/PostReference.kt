package backend.core.reference

import backend.core.types.PostId
import backend.core.types.UserId
import kotlinx.datetime.LocalDateRange
import kotlin.time.Instant


sealed interface PostReference {
    data class Id(val id: PostId) : PostReference
    data class FirstPost(val self: UserId) : PostReference
    data object Random : PostReference
    data class RandomAuthor(val self: UserId) : PostReference
    data class LastPost(val self: UserId) : PostReference
}
