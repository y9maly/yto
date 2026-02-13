package presentation.api.krpc

import backend.core.input.InputPostLocation
import backend.core.reference.PostReference
import backend.core.reference.UserReference
import backend.core.reference.ref
import domain.service.MainService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import presentation.assembler.MainAssembler
import presentation.assembler.PostAssembler
import presentation.assembler.postAssembler
import domain.service.result.CreatePostError as DomainCreatePostError
import presentation.authenticator.Authenticator
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.authStateOrPut
import presentation.integration.callContext.elements.sessionId
import presentation.presenter.MainPresenter
import y9to.api.krpc.PostRpc
import y9to.api.types.CreatePostError
import y9to.api.types.CreatePostResult
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.Post
import y9to.api.types.Token
import y9to.api.types.UserId
import y9to.libs.stdlib.Slice
import y9to.libs.stdlib.SpliceKey
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.mapList
import y9to.libs.stdlib.mapOptions
import y9to.libs.stdlib.successOrElse


class PostRpcImpl(
    private val authenticator: Authenticator,
    private val service: MainService,
    private val assembler: MainAssembler,
    private val presenter: MainPresenter,
) : PostRpc {
    override suspend fun get(token: Token, input: InputPost): Post? = authenticate(token) {
        val postRef = assembler.post.resolve(input)
            ?: return@authenticate null
        val post = service.post.get(postRef) ?: return null
        return presenter.post.Post(post)
    }

    override suspend fun create(
        token: Token,
        replyTo: InputPost?,
        content: InputPostContent
    ): CreatePostResult = authenticate(token) {
        val authState = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: return@authenticate CreatePostError.Unauthorized.asError()
        }

        val userRef = authState.userIdOrNull()?.ref()
            ?: return@authenticate CreatePostError.Unauthorized.asError()

        val replyToPost = replyTo?.let { assembler.post.resolve(replyTo) }

        val content = assembler.post.InputPostContent(content)
            ?: return@authenticate CreatePostError.InvalidInputContent.asError()

        val post = service.post.create(InputPostLocation.Global, userRef, replyToPost, content)
            .successOrElse { error ->
                return when (error) {
                    DomainCreatePostError.InvalidInputContent -> CreatePostError.InvalidInputContent
                    DomainCreatePostError.UnknownReplyToPostReference -> CreatePostError.UnknownReplyOption
                    DomainCreatePostError.UnknownAuthorReference -> CreatePostError.Unauthorized
                    DomainCreatePostError.InvalidInputLocation -> error("Unreachable")
                }.asError()
            }

        val remotePost = presenter.post.Post(post)
        remotePost.asOk()
    }

    override suspend fun sliceGlobal(
        token: Token,
        key: SpliceKey<Unit>,
        limit: Int
    ): Slice<Post> = authenticate(token) {
        coroutineScope {
            service.post.sliceGlobal(key, limit)
                .mapList {
                    async { presenter.post.Post(it) }
                }
                .mapList {
                    it.await()
                }
        }
    }

    override suspend fun sliceProfile(
        token: Token,
        key: SpliceKey<UserId>,
        limit: Int,
    ): Slice<Post>? = authenticate(token) {
        coroutineScope {
            service.post.sliceProfile(
                key = key.mapOptions { assembler.user.UserId(it) },
                limit = limit,
            )
                ?.mapList {
                    async { presenter.post.Post(it) }
                }
                ?.mapList {
                    it.await()
                }
        }
    }

    private suspend inline fun <R> authenticate(token: Token, block: CallContext.() -> R) =
        presentation.api.krpc.internals.authenticate(authenticator, token) {
            postAssembler = assembler.post
            block()
        }
}

