@file:JvmName("ResultPostKt")

package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>


typealias CreatePostOk = Post


@S sealed interface CreatePostError {
    @SerialName("Unauthorized")
    @S data object Unauthorized : CreatePostError

    @SerialName("InvalidInputReply")
    @S data object InvalidInputReply : CreatePostError

    @SerialName("InvalidInputContent")
    @S data object InvalidInputContent : CreatePostError
}
