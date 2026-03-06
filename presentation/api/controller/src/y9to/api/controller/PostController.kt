package y9to.api.controller

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable as S
import presentation.integration.context.Context
import y9to.api.types.*
import y9to.libs.paging.Cursor
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey


interface PostController {
    context(_: Context) suspend fun get(input: InputPost): Post?

    context(_: Context) suspend fun create(
        location: InputPostLocation,
        replyTo: InputPost?,
        content: InputPostContent,
    ): CreatePostResult

    context(_: Context) suspend fun sliceFeed(
        key: SliceKey<InputFeed, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post>
}
