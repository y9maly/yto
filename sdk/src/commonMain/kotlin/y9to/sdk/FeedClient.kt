package y9to.sdk

import kotlinx.coroutines.Dispatchers
import y9to.api.types.Post
import y9to.libs.stdlib.Slice
import y9to.libs.stdlib.SpliceKey
import y9to.libs.stdlib.mapOptions
import y9to.sdk.types.FeedPagingOptions


class FeedClient internal constructor(val client: Client) {
    suspend fun splice(
        key: SpliceKey<FeedPagingOptions>,
        limit: Int,
    ): Slice<Post> {
        return client.rpc.post.sliceGlobal(
            client.token,
            key.mapOptions {  },
            limit,
        )
    }
}
