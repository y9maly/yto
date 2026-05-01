package integration.repository.result

import backend.core.types.Post
import y9to.libs.stdlib.Union


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>
typealias EditPostResult = Union<EditPostOk, EditPostError>
typealias DeletePostResult = Union<DeletePostOk, DeletePostError>


typealias CreatePostOk = Post
typealias EditPostOk = Post
typealias DeletePostOk = Unit


sealed interface CreatePostError {
    data object InvalidAuthorId : CreatePostError
    data object InvalidReplyToPostId : CreatePostError
    data object InvalidInputContent : CreatePostError
    data object InvalidInputLocation : CreatePostError
}

sealed interface EditPostError {
    data object InvalidPostId : EditPostError
    data object InvalidNewAuthorId : EditPostError
    data object InvalidNewReplyTo : EditPostError
    data object InvalidNewInputContent : EditPostError
}

sealed interface DeletePostError {
    data object InvalidPostId : DeletePostError
}
