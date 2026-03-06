package y9to.api.y9to.api.endpoint

import y9to.api.types.*
import y9to.libs.paging.Cursor
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey


interface PostEndpoint {
    fun get(input: InputPost): Post?

    fun create(
        location: InputPostLocation,
        replyTo: InputPost?,
        content: InputPostContent,
    ): CreatePostResult

    fun sliceFeed(
        key: SliceKey<InputFeed, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post>
}
