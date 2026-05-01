package domain.service.result

import domain.service.result.internals.mapError


@JvmName("mapCreatePostResult")
internal fun integration.repository.result.CreatePostResult.map() = mapError { map() }
internal fun integration.repository.result.CreatePostError.map() = when (this) {
    integration.repository.result.CreatePostError.InvalidInputContent -> CreatePostError.InvalidInputContent
    integration.repository.result.CreatePostError.InvalidAuthorId -> CreatePostError.InvalidAuthorId
    integration.repository.result.CreatePostError.InvalidReplyToPostId -> CreatePostError.InvalidReplyTo
    integration.repository.result.CreatePostError.InvalidInputLocation -> CreatePostError.InvalidInputLocation
}

@JvmName("mapEditPostResult")
internal fun integration.repository.result.EditPostResult.map() = mapError { map() }
internal fun integration.repository.result.EditPostError.map() = when (this) {
    integration.repository.result.EditPostError.InvalidPostId -> EditPostError.InvalidPostId
    integration.repository.result.EditPostError.InvalidNewAuthorId -> EditPostError.InvalidNewAuthorId
    integration.repository.result.EditPostError.InvalidNewReplyTo -> EditPostError.InvalidNewReplyTo
    integration.repository.result.EditPostError.InvalidNewInputContent -> EditPostError.InvalidNewInputContent
}

@JvmName("mapDeletePostResult")
internal fun integration.repository.result.DeletePostResult.map(): DeletePostResult = mapError { map() }
internal fun integration.repository.result.DeletePostError.map(): DeletePostError = when (this) {
    integration.repository.result.DeletePostError.InvalidPostId -> DeletePostError.InvalidPostId
}
