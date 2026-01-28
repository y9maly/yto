@file:JvmName("ResultPostKt")

package y9to.api.types

import kotlinx.serialization.Serializable
import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>


typealias CreatePostOk = Post


@Serializable
sealed interface CreatePostError {
    @Serializable
    data object Unauthorized : CreatePostError
    @Serializable
    data object UnknownReplyOption : CreatePostError
    @Serializable
    data object InvalidInputContent : CreatePostError
}
