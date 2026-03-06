package domain.service.result

import backend.core.types.Post
import domain.service.result.internals.mapError
import integration.repository.result.InsertPostError
import integration.repository.result.InsertPostResult
import y9to.libs.stdlib.Union


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>
typealias DeletePostResult = Union<DeletePostOk, DeletePostError>
typealias GetAuthorPostResult = Union<GetAuthorPostOk, GetAuthorPostError>


typealias CreatePostOk = Post
typealias DeletePostOk = Unit
typealias GetAuthorPostOk = Post


sealed interface CreatePostError {
    data object InvalidAuthorRef : CreatePostError
    data object InvalidReplyRef : CreatePostError
    data object InvalidInputContent : CreatePostError
    data object InvalidInputLocation : CreatePostError
}

sealed interface DeletePostError {
    data object InvalidPostRef : DeletePostError
}

sealed interface GetAuthorPostError {
    data object InvalidAuthorRef : GetAuthorPostError
    data object AuthorHasNoPosts : GetAuthorPostError
}


fun InsertPostResult.map() = mapError { map() }
fun InsertPostError.map() = when (this) {
    InsertPostError.InvalidInputContent -> CreatePostError.InvalidInputContent
    InsertPostError.InvalidAuthorLink -> CreatePostError.InvalidAuthorRef
    InsertPostError.InvalidReplyToPostLink -> CreatePostError.InvalidReplyRef
    InsertPostError.InvalidInputLocation -> CreatePostError.InvalidInputLocation
}
