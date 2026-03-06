package integration.repository.input

import backend.core.types.Filter
import backend.core.types.PostContentType
import backend.core.types.PostId
import backend.core.types.PostReference
import backend.core.types.UserId
import backend.core.types.UserReference
import integration.repository.input.InputPostContent.Repost
import integration.repository.input.InputPostContent.Standalone
import kotlinx.serialization.Serializable


sealed interface InputPostLocation {
    data object Global : InputPostLocation
    data class Profile(val user: UserReference) : InputPostLocation
}

sealed interface InputPostContent {
    data class Standalone(val text: String) : InputPostContent
    data class Repost(val comment: String?, val original: PostReference) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is Standalone -> PostContentType.Standalone
    is Repost -> PostContentType.Repost
}
