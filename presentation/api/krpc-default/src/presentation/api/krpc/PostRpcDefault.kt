package presentation.api.krpc

import presentation.api.krpc.internals.authenticate
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import y9to.api.controller.PostController
import y9to.api.krpc.PostRpc
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.Token
import y9to.api.types.UserId
import y9to.libs.paging.SliceKey


class PostRpcDefault(
    private val authenticator: Authenticator,
    private val controller: PostController
) : PostRpc {
    override suspend fun get(token: Token, input: InputPost) =
        authenticate(token) { get(input) }

    override suspend fun create(
        token: Token,
        replyTo: InputPost?,
        content: InputPostContent
    ) = authenticate(token) { create(replyTo, content) }

    override suspend fun sliceGlobal(
        token: Token,
        key: SliceKey<Unit>,
        limit: Int
    ) = authenticate(token) { sliceGlobal(key, limit) }

    override suspend fun sliceProfile(
        token: Token,
        key: SliceKey<UserId>,
        limit: Int,
    ) = authenticate(token) { sliceProfile(key, limit) }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context) PostController.() -> R) =
        authenticate(authenticator, token) { block(this, controller) }
}
