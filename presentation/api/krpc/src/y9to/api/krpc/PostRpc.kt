package y9to.api.krpc

import kotlinx.rpc.annotations.Rpc
import y9to.api.types.*
import y9to.libs.stdlib.Slice
import y9to.libs.stdlib.SpliceKey


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
        key: SpliceKey<Unit>,
        limit: Int,
    ): Slice<Post>

    suspend fun sliceProfile(
        token: Token,
        key: SpliceKey<UserId>,
        limit: Int,
    ): Slice<Post>?
}

