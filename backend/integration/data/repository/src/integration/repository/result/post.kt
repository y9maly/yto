package integration.repository.result

import backend.core.types.Post
import y9to.libs.stdlib.Union


typealias CreatePostResult = Union<InsertPostOk, CreatePostError>
typealias DeletePostResult = Union<DeletePostOk, DeletePostError>


typealias InsertPostOk = Post
typealias DeletePostOk = Unit


sealed interface CreatePostError {
    data object InvalidAuthorLink : CreatePostError
    data object InvalidReplyToPostLink : CreatePostError
    data object InvalidInputContent : CreatePostError
    data object InvalidInputLocation : CreatePostError
}

sealed interface DeletePostError {
    data object InvalidPostId : DeletePostError
}
