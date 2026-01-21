package backend.core.reference

import backend.core.types.PostId
import backend.core.types.UserId


sealed interface PostReference {
    data class Id(val id: PostId) : PostReference
    data class FirstAuthor(val self: UserReference) : PostReference
    data class RandomAuthor(val self: UserReference) : PostReference
    data class LastAuthor(val self: UserReference) : PostReference
    data object First : PostReference
    data object Random : PostReference
    data object Last : PostReference
}
