package y9to.api.krpc

import kotlinx.rpc.annotations.Rpc
import y9to.api.types.*
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey


@Rpc
interface PostRpc {
    suspend fun get(token: Token, input: InputPost): Post?

    suspend fun create(
        token: Token,
        replyTo: InputPost?,
        content: InputPostContent,
    ): CreatePostResult

    suspend fun sliceGlobal(
        token: Token,
        key: SliceKey<Unit>,
        limit: Int,
    ): Slice<Post>

    suspend fun sliceProfile(
        token: Token,
        key: SliceKey<UserId>,
        limit: Int,
    ): Slice<Post>?
}

