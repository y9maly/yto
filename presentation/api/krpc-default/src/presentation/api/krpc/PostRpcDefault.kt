package presentation.api.krpc

import presentation.api.krpc.internals.authenticate
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import y9to.api.controller.PostController
import y9to.api.krpc.PostRpc
import y9to.api.types.*
import y9to.libs.paging.Cursor
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey
import y9to.libs.stdlib.optional.Optional


class PostRpcDefault(
    private val authenticator: Authenticator,
    private val controller: PostController
) : PostRpc {
    override suspend fun resolve(token: Token, input: InputPost) =
        authenticate(token) { resolve(input) }

    override suspend fun get(token: Token, input: InputPost) =
        authenticate(token) { get(input) }

    override suspend fun create(
        token: Token,
        location: InputPostLocation,
        replyTo: InputPost?,
        content: InputPostContent,
    ) = authenticate(token) { create(location, replyTo, content) }

    override suspend fun edit(
        token: Token,
        post: InputPost,
        replyTo: Optional<InputPost?>,
        content: Optional<InputPostContent>
    ) = authenticate(token) { edit(post, replyTo, content) }

    override suspend fun delete(token: Token, post: InputPost) =
        authenticate(token) { delete(post) }

    override suspend fun sliceFeed(
        token: Token,
        key: SliceKey<InputFeed, Cursor>,
        limit: Int,
    ) = authenticate(token) { sliceFeed(key, limit) }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context) PostController.() -> R) =
        authenticate(authenticator, token) { block(this, controller) }
}
