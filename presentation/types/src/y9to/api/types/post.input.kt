package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmName


@S sealed interface InputFeed {
    @SerialName("Global")
    @S data object Global : InputFeed

    @SerialName("Profile")
    @S data class Profile(val user: UserId) : InputFeed
}

@S sealed interface InputPost {
    @SerialName("MyLastPost")
    @S data object MyLastPost : InputPost

    @SerialName("MyFirstPost")
    @S data object MyFirstPost : InputPost

    @SerialName("MyRandomPost")
    @S data object MyRandomPost : InputPost
}

@S sealed interface InputPostLocation {
    @SerialName("Global")
    @S data object Global : InputPostLocation

    @SerialName("Profile")
    @S data class Profile(val user: InputUser) : InputPostLocation
}

@S sealed interface InputPostContent {
    @SerialName("Standalone")
    @S data class Standalone(val text: String) : InputPostContent

    @SerialName("Repost")
    @S data class Repost(val comment: String?, val original: InputPost) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is InputPostContent.Standalone -> PostContentType.Standalone
    is InputPostContent.Repost -> PostContentType.Repost
}
