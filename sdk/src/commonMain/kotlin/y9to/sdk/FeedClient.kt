package y9to.sdk

import y9to.api.types.Post
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey
import y9to.libs.paging.mapOptions
import y9to.sdk.types.FeedPagingOptions


class FeedClient internal constructor(val client: Client) {
    suspend fun slice(
        key: SliceKey<FeedPagingOptions>,
        limit: Int,
    ): Slice<Post> {
        return client.rpc.post.sliceGlobal(
            client.token,
            key.mapOptions {  },
            limit,
        )
    }
}
