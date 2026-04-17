package y9to.sdk

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import y9to.api.types.CreatePostResult
import y9to.api.types.InputFeed
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.InputPostLocation
import y9to.api.types.Post
import y9to.libs.paging.Cursor
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey
import y9to.sdk.internals.ClientOwner
import y9to.sdk.internals.request


class PostClient internal constructor(override val client: Client) : ClientOwner {
    fun getFlow(input: InputPost): Flow<Post?> = flow {
        while (true) {
            try {
                emit(get(input))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            } finally {
                delay((1500..3000L).random())
            }
        }
    }

    suspend fun get(input: InputPost): Post? {
        return request {
            rpc.post.get(token, input)
        }
    }

    suspend fun create(
        location: InputPostLocation,
        replyTo: InputPost? = null,
        content: InputPostContent,
    ): CreatePostResult {
        return request {
            rpc.post.create(
                token = token,
                location = location,
                replyTo = replyTo,
                content = content,
            )
        }
    }

    suspend fun sliceFeed(
        key: SliceKey<InputFeed, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post> {
        return request {
            rpc.post.sliceFeed(
                token = token,
                key = key,
                limit = limit,
            )
        }
    }
}
