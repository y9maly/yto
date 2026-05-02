package y9to.sdk

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import y9to.api.types.ApiUpdateSubscription
import y9to.api.types.CreatePostResult
import y9to.api.types.DeletePostResult
import y9to.api.types.EditPostResult
import y9to.api.types.InputFeed
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.InputPostLocation
import y9to.api.types.Post
import y9to.api.types.PostId
import y9to.api.types.Update
import y9to.libs.paging.Cursor
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.getOrElse
import y9to.sdk.internals.ClientOwner
import y9to.sdk.internals.request


class PostClient internal constructor(override val client: Client) : ClientOwner {
    suspend fun resolve(input: InputPost): PostId? =
        input as? PostId ?: request("PostClient#resolve(input=$input)") { rpc.post.resolve(token, input) }

    fun getFlow(input: InputPost): Flow<Post?> = channelFlow {
        val postId = resolve(input)
            ?: run {
                send(null)
                return@channelFlow
            }

        val updates = client.updateCenter.saveIn(this)
        client.updateCenter.subscribe(ApiUpdateSubscription.PostEdited(postId))

        try {
            var post = request("PostClient#getFlow(postId=${postId.long})") { get(postId) }
            send(post)

            updates.filterIsInstance<Update.PostEdited>().collect { update ->
                if (post == null) {
                    post = get(postId)
                    return@collect
                }

                if (update.post != postId)
                    return@collect
                if (update.post == post)
                    return@collect

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
        return request("PostClient#get(input=$input)") {
            rpc.post.get(token, input)
        }
    }

    suspend fun create(
        location: InputPostLocation,
        replyTo: InputPost? = null,
        content: InputPostContent,
    ): CreatePostResult {
        return request("PostClient#create") {
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
        return request("PostClient#edit") { rpc.post.edit(
            token = token,
            post = post,
            replyTo = replyTo,
            content = content,
        ) }
    }

    suspend fun delete(post: InputPost): DeletePostResult {
        return request("PostClient#delete(input=$post)") { rpc.post.delete(token, post) }
    }

    suspend fun sliceFeed(
        key: SliceKey<InputFeed, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post> {
        return request("PostClient#sliceFeed(key=$key)") {
            rpc.post.sliceFeed(
                token = token,
                key = key,
                limit = limit,
            )
        }
    }
}
