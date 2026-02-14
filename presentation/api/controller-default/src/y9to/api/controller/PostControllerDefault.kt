package y9to.api.controller

import backend.core.types.InputPostLocation
import backend.core.types.ref
import domain.service.MainService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import presentation.assembler.MainAssembler
import presentation.assembler.map
import presentation.assembler.resolve
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.presenter.MainPresenter
import presentation.presenter.map
import y9to.api.types.CreatePostError
import y9to.api.types.CreatePostResult
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.Post
import y9to.api.types.UserId
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey
import y9to.libs.paging.mapList
import y9to.libs.paging.mapOptions
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.successOrElse
import domain.service.result.CreatePostError as DomainCreatePostError


class PostControllerDefault(
    private val service: MainService,
    override val assembler: MainAssembler,
    override val presenter: MainPresenter,
) : PostController, ControllerDefault {
    context(_: Context)
    override suspend fun get(input: InputPost): Post? = context {
        val postRef = input.resolve() ?: return null
        val post = service.post.get(postRef) ?: return null
        return post.map()
    }

    context(_: Context)
    override suspend fun create(
        replyTo: InputPost?,
        content: InputPostContent
    ): CreatePostResult = context {
        val authState = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: return CreatePostError.Unauthorized.asError()
        }

        val userRef = authState.userIdOrNull()?.ref()
            ?: return CreatePostError.Unauthorized.asError()

        val replyToPost = replyTo?.resolve()

        val content = content.map()
            ?: return CreatePostError.InvalidInputContent.asError()

        val post = service.post.create(InputPostLocation.Global, userRef, replyToPost, content)
            .successOrElse { error ->
                return when (error) {
                    DomainCreatePostError.InvalidInputContent -> CreatePostError.InvalidInputContent
                    DomainCreatePostError.UnknownReplyToPostReference -> CreatePostError.UnknownReplyOption
                    DomainCreatePostError.UnknownAuthorReference -> CreatePostError.Unauthorized
                    DomainCreatePostError.InvalidInputLocation -> error("Unreachable")
                }.asError()
            }

        return post.map().asOk()
    }

    context(_: Context)
    override suspend fun sliceGlobal(
        key: SliceKey<Unit>,
        limit: Int
    ): Slice<Post> = context {
        return coroutineScope {
            service.post.sliceGlobal(key, limit)
                .mapList {
                    async { it.map() }
                }
                .mapList {
                    it.await()
                }
        }
    }

    context(_: Context)
    override suspend fun sliceProfile(
        key: SliceKey<UserId>,
        limit: Int
    ): Slice<Post>? = context {
        return coroutineScope {
            service.post.sliceProfile(
                key = key.mapOptions { it.map() },
                limit = limit,
            )
                ?.mapList {
                    async { it.map() }
                }
                ?.mapList {
                    it.await()
                }
        }
    }
}
