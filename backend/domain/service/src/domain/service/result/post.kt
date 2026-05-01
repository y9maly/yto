package domain.service.result

import backend.core.types.Post
import y9to.libs.stdlib.Union


typealias CreatePostResult = Union<CreatePostOk, CreatePostError>
typealias EditPostResult = Union<EditPostOk, EditPostError>
typealias DeletePostResult = Union<DeletePostOk, DeletePostError>
typealias GetAuthorPostResult = Union<GetAuthorPostOk, GetAuthorPostError>


typealias CreatePostOk = Post
typealias EditPostOk = Post
typealias DeletePostOk = Unit
typealias GetAuthorPostOk = Post


sealed interface CreatePostError {
    data object InvalidAuthorId : CreatePostError
    data object InvalidReplyTo : CreatePostError
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

sealed interface GetAuthorPostError {
    data object InvalidAuthorRef : GetAuthorPostError
    data object AuthorHasNoPosts : GetAuthorPostError
}
