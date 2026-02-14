package backend.core.types


sealed interface InputPostLocation {
    data object Global : InputPostLocation
    data class Profile(val user: UserReference) : InputPostLocation
}

sealed interface InputPostContent {
    data class Standalone(val text: String) : InputPostContent
    data class Repost(val comment: String?, val original: PostReference) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is InputPostContent.Standalone -> PostContentType.Standalone
    is InputPostContent.Repost -> PostContentType.Repost
}
