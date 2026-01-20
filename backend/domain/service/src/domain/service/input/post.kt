package domain.service.input

import backend.core.types.PostContentType
import backend.core.types.PostId


sealed interface InputPostContent {
    data class Standalone(val text: String) : InputPostContent
    data class Repost(val comment: String?, val original: PostId) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is InputPostContent.Standalone -> PostContentType.Standalone
    is InputPostContent.Repost -> PostContentType.Repost
}



fun InputPostContent.map() = when (this) {
    is InputPostContent.Repost -> integration.repository.input.InputPostContent.Repost(comment, original)
    is InputPostContent.Standalone -> integration.repository.input.InputPostContent.Standalone(text)
}
