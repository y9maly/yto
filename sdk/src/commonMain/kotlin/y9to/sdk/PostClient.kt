package y9to.sdk

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import y9to.api.types.ApiUpdateSubscription
import y9to.api.types.CreatePostResult
import y9to.api.types.EditPostResult
import y9to.api.types.InputFeed
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.InputPostLocation
import y9to.api.types.Post
import y9to.api.types.Update
import y9to.libs.paging.Cursor
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.getOrElse
import y9to.sdk.internals.ClientOwner
import y9to.sdk.internals.request


class PostClient internal constructor(override val client: Client) : ClientOwner {
    fun getFlow(input: InputPost): Flow<Post?> = channelFlow {
        var post: Post? = request { rpc.post.get(token, input) }
            ?: run {
                send(null)
                return@channelFlow
            }
        val postId = post!!.id
        send(post)

        client.updateCenter.subscribe(ApiUpdateSubscription.PostEdited(postId))

        try {
            client.updateCenter.updates.filterIsInstance<Update.PostEdited>().collect { update ->
                if (post == null) {
                    post = get(postId)
                    return@collect
                }

                if (update.post != postId)
                    return@collect
                post = post.copy(content = update.newContent)

                if (update.newAuthor.isPresent || update.newReplyTo.isPresent) {
                    post = get(postId)
                } else {
                    post = post!!.copy(
                        content = update.newContent.getOrElse { post!!.content }
                    )
                }

                send(post)
            }
        } finally {
            client.updateCenter.unsubscribe(ApiUpdateSubscription.PostEdited(postId))
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

    suspend fun edit(
        post: InputPost,
        replyTo: Optional<InputPost?>,
        content: Optional<InputPostContent>
    ): EditPostResult {
        return request { rpc.post.edit(
            token = token,
            post = post,
            replyTo = replyTo,
            content = content,
        ) }
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
