package domain.service.result

import backend.core.types.Post
import y9to.libs.stdlib.Union


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>
typealias DeletePostResult = Union<DeletePostOk, DeletePostError>
typealias GetAuthorPostResult = Union<GetAuthorPostOk, GetAuthorPostError>


typealias CreatePostOk = Post
typealias DeletePostOk = Unit
typealias GetAuthorPostOk = Post


sealed interface CreatePostError {
    data object InvalidAuthorId : CreatePostError
    data object InvalidReplyId : CreatePostError
    data object InvalidInputContent : CreatePostError
    data object InvalidInputLocation : CreatePostError
}

sealed interface DeletePostError {
    data object InvalidPostId : DeletePostError
}

sealed interface GetAuthorPostError {
    data object InvalidAuthorRef : GetAuthorPostError
    data object AuthorHasNoPosts : GetAuthorPostError
}
