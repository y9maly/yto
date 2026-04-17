package domain.service.result

import domain.service.result.internals.mapError


@JvmName("mapCreatePostResult")
internal fun integration.repository.result.CreatePostResult.map() = mapError { map() }
internal fun integration.repository.result.CreatePostError.map() = when (this) {
    integration.repository.result.CreatePostError.InvalidInputContent -> CreatePostError.InvalidInputContent
    integration.repository.result.CreatePostError.InvalidAuthorLink -> CreatePostError.InvalidAuthorId
    integration.repository.result.CreatePostError.InvalidReplyToPostLink -> CreatePostError.InvalidReplyId
    integration.repository.result.CreatePostError.InvalidInputLocation -> CreatePostError.InvalidInputLocation
}

@JvmName("mapDeletePostResult")
internal fun integration.repository.result.DeletePostResult.map(): DeletePostResult = mapError { map() }
internal fun integration.repository.result.DeletePostError.map(): DeletePostError = when (this) {
    integration.repository.result.DeletePostError.InvalidPostId -> DeletePostError.InvalidPostId
}
