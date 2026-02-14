@file:JvmName("ResultPostKt")

package y9to.api.types

import kotlinx.serialization.Serializable as S
import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>


typealias CreatePostOk = Post


@S sealed interface CreatePostError {
    @S data object Unauthorized : CreatePostError
    @S data object UnknownReplyOption : CreatePostError
    @S data object InvalidInputContent : CreatePostError
}
