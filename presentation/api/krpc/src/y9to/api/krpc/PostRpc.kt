package y9to.api.krpc

import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable
import y9to.api.types.*
import y9to.libs.paging.Cursor
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none


@Rpc
interface PostRpc {
    suspend fun resolve(token: Token, input: InputPost): PostId?

    suspend fun get(token: Token, input: InputPost): Post?

    suspend fun create(
        token: Token,
        location: InputPostLocation,
        replyTo: InputPost?,
        content: InputPostContent,
    ): CreatePostResult

    suspend fun edit(
        token: Token,
        post: InputPost,
        replyTo: Optional<InputPost?> = none(),
        content: Optional<InputPostContent> = none(),
    ): EditPostResult

    suspend fun delete(token: Token, post: InputPost): DeletePostResult

    suspend fun sliceFeed(
        token: Token,
        key: SliceKey<InputFeed, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post>
}
