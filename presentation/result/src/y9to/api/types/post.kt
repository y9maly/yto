@file:JvmName("ResultPostKt")

package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>
typealias EditPostResult = Union<EditPostOk, EditPostError>


typealias EditPostOk = Post
typealias CreatePostOk = Post


@S sealed interface CreatePostError {
    @SerialName("Unauthorized")
    @S data object Unauthorized : CreatePostError

    @SerialName("InvalidInputReplyTo")
    @S data object InvalidInputReplyTo : CreatePostError

    @SerialName("InvalidInputContent")
    @S data object InvalidInputContent : CreatePostError
}

@S sealed interface EditPostError {
    @SerialName("Unauthorized")
    @S data object Unauthorized : EditPostError

    @SerialName("AccessDenied")
    @S data object AccessDenied : EditPostError

    @SerialName("InvalidInputPost")
    @S data object InvalidInputPost : EditPostError

    @SerialName("InvalidNewInputReplyTo")
    @S data object InvalidNewInputReplyTo : EditPostError

    @SerialName("InvalidNewInputContent")
    @S data object InvalidNewInputContent : EditPostError
}
