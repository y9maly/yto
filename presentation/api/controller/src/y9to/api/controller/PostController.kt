package y9to.api.controller

import presentation.integration.context.Context
import y9to.api.types.CreatePostResult
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.Post
import y9to.api.types.Token
import y9to.api.types.UserId
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey


interface PostController {
    context(_: Context) suspend fun get(input: InputPost): Post?

    context(_: Context) suspend fun create(
        replyTo: InputPost?,
        content: InputPostContent,
    ): CreatePostResult

    context(_: Context) suspend fun sliceGlobal(
        key: SliceKey<Unit>,
        limit: Int,
    ): Slice<Post>

    context(_: Context) suspend fun sliceProfile(
        key: SliceKey<UserId>,
        limit: Int,
    ): Slice<Post>?
}
