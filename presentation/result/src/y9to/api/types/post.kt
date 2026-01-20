package y9to.api.types

import y9to.libs.stdlib.Union


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>


typealias CreatePostOk = Post


sealed interface CreatePostError {
    data object Unauthorized : CreatePostError
    data object UnknownReplyOption : CreatePostError
    data object InvalidInputContent : CreatePostError
}
