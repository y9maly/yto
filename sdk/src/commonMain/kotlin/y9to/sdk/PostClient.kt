package y9to.sdk

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import y9to.api.types.CreatePostResult
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.Post


class PostClient internal constructor(private val client: Client) {
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
        return client.rpc.post.get(client.token, input)
    }

    suspend fun create(
        replyTo: InputPost? = null,
        content: InputPostContent,
    ): CreatePostResult {
        return client.rpc.post.create(
            token = client.token,
            replyTo = replyTo,
            content = content,
        )
    }
}
