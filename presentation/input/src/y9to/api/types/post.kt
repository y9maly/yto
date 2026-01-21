package y9to.api.types

import kotlinx.serialization.Serializable


@Serializable
sealed interface InputPost {
    @Serializable
    data class Id(val id: PostId) : InputPost
    @Serializable
    data object MyLastPost : InputPost
    @Serializable
    data object MyFirstPost : InputPost
    @Serializable
    data object MyRandomPost : InputPost
//    data class Access(val access: PostAccess) : InputPost
}

@Serializable
sealed interface InputPostContent {
    @Serializable
    data class Standalone(val text: String) : InputPostContent
    @Serializable
    data class Repost(val comment: String?, val original: InputPost) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is InputPostContent.Standalone -> PostContentType.Standalone
    is InputPostContent.Repost -> PostContentType.Repost
}
