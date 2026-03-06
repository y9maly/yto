package integration.repository.result

import backend.core.types.Post
import y9to.libs.stdlib.Union


typealias InsertPostResult = Union<InsertPostOk, InsertPostError>
typealias DeletePostResult = Union<DeletePostOk, DeletePostError>
typealias SelectAuthorPostResult = Union<SelectAuthorPostOk, SelectAuthorPostError>


typealias InsertPostOk = Post
typealias DeletePostOk = Unit
typealias SelectAuthorPostOk = Post


sealed interface InsertPostError {
    data object InvalidAuthorLink : InsertPostError
    data object InvalidReplyToPostLink : InsertPostError
    data object InvalidInputContent : InsertPostError
    data object InvalidInputLocation : InsertPostError
}

sealed interface DeletePostError {
    data object InvalidPostLink : DeletePostError
}

sealed interface SelectAuthorPostError {
    data object InvalidAuthorLink : SelectAuthorPostError
    data object AuthorHasNoPosts : SelectAuthorPostError
}
