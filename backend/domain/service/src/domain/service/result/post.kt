package domain.service.result

import backend.core.types.Post
import domain.service.result.internals.mapError
import integration.repository.result.InsertPostError
import integration.repository.result.InsertPostResult
import integration.repository.result.LogInError
import integration.repository.result.LogInResult
import integration.repository.result.LogOutError
import integration.repository.result.LogOutResult
import y9to.libs.stdlib.Union


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>
typealias GetAuthorPostResult = Union<GetAuthorPostOk, GetAuthorPostError>


typealias CreatePostOk = Post
typealias GetAuthorPostOk = Post


sealed interface CreatePostError {
    data object UnknownAuthorReference : CreatePostError
    data object UnknownReplyToPostReference : CreatePostError
    data object InvalidInputContent : CreatePostError
    data object InvalidInputLocation : CreatePostError
}

sealed interface GetAuthorPostError {
    data object UnknownAuthorId : GetAuthorPostError
    data object AuthorHasNoPosts : GetAuthorPostError
}


fun InsertPostResult.map() = mapError { map() }
fun InsertPostError.map() = when (this) {
    InsertPostError.InvalidInputContent -> CreatePostError.InvalidInputContent
    InsertPostError.UnknownAuthorId -> CreatePostError.UnknownAuthorReference
    InsertPostError.UnknownReplyToPostId -> CreatePostError.UnknownReplyToPostReference
    InsertPostError.InvalidInputLocation -> CreatePostError.InvalidInputLocation
}
