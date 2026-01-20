package y9to.api.types


sealed interface InputPost {
    data class Id(val id: PostId) : InputPost
    data object MyLastPost : InputPost
    data object MyFirstPost : InputPost
    data object MyRandomPost : InputPost
//    data class Access(val access: PostAccess) : InputPost
}

sealed interface InputPostContent {
    data class Standalone(val text: String) : InputPostContent
    data class Repost(val comment: String?, val original: InputPost) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is InputPostContent.Standalone -> PostContentType.Standalone
    is InputPostContent.Repost -> PostContentType.Repost
}
