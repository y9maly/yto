@file:JvmName("InputPostKt")

package y9to.api.types

import kotlinx.serialization.Serializable as S
import kotlin.jvm.JvmName


@S sealed interface InputPost {
    @S data class Id(val id: PostId) : InputPost
    @S data object MyLastPost : InputPost
    @S data object MyFirstPost : InputPost
    @S data object MyRandomPost : InputPost
//    data class Access(val access: PostAccess) : InputPost
}

@S sealed interface InputPostContent {
    @S data class Standalone(val text: String) : InputPostContent
    @S data class Repost(val comment: String?, val original: InputPost) : InputPostContent
}

val InputPostContent.type get() = when (this) {
    is InputPostContent.Standalone -> PostContentType.Standalone
    is InputPostContent.Repost -> PostContentType.Repost
}
