package integration.repository.result

import backend.core.types.Post
import y9to.libs.stdlib.Union


typealias InsertPostResult = Union<InsertPostOk, InsertPostError>
typealias SelectAuthorPostResult = Union<SelectAuthorPostOk, SelectAuthorPostError>


typealias InsertPostOk = Post
typealias SelectAuthorPostOk = Post


sealed interface InsertPostError {
    data object UnknownAuthorId : InsertPostError
    data object UnknownReplyToPostId : InsertPostError
    data object InvalidInputContent : InsertPostError
    data object InvalidInputLocation : InsertPostError
}

sealed interface SelectAuthorPostError {
    data object UnknownAuthorId : SelectAuthorPostError
    data object AuthorHasNoPosts : SelectAuthorPostError
}
