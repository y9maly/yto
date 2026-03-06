package integration.repository.input

import backend.core.types.PostContentType
import backend.core.types.PostLink
import backend.core.types.UserLink
import integration.repository.input.InputPostContent.Repost
import integration.repository.input.InputPostContent.Standalone


sealed interface InputPostLocation {
    data object Global : InputPostLocation
    data class Profile(val user: UserLink) : InputPostLocation
}

sealed interface InputPostContent {
    data class Standalone(val text: String) : InputPostContent
    data class Repost(val comment: String?, val original: PostLink) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is Standalone -> PostContentType.Standalone
    is Repost -> PostContentType.Repost
}
