package integration.repository.input

import backend.core.types.PostId
import integration.repository.input.InputPostContent.Repost
import integration.repository.input.InputPostContent.Standalone
import backend.core.types.PostContentType


sealed interface InputPostContent {
    data class Standalone(val text: String) : InputPostContent
    data class Repost(val comment: String?, val original: PostId) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is Standalone -> PostContentType.Standalone
    is Repost -> PostContentType.Repost
}
