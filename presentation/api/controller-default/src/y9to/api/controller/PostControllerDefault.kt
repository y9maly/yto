package y9to.api.controller

import backend.core.types.link
import domain.service.MainService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import presentation.assembler.MainAssembler
import presentation.assembler.map
import presentation.assembler.resolve
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.presenter.MainPresenter
import presentation.presenter.map
import y9to.api.types.*
import y9to.libs.paging.*
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.successOrElse
import domain.service.result.CreatePostError as DomainCreatePostError


class PostControllerDefault(
    private val service: MainService,
    override val assembler: MainAssembler,
    override val presenter: MainPresenter,
) : PostController, ControllerDefault {
    @Serializable
    private data class CursorPayload(
        val feed: InputFeed,
        val serviceCursor: Cursor,
    )

    context(_: Context)
    override suspend fun get(input: InputPost): Post? = context {
        val postLink = input.resolve() ?: return null
        val post = service.post.get(postLink) ?: return null
        return post.map()
    }

    context(_: Context)
    override suspend fun create(
        location: InputPostLocation,
        replyTo: InputPost?,
        content: InputPostContent
    ): CreatePostResult = context {
        val authState = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: return CreatePostError.Unauthorized.asError()
        }

        val userLink = authState.userIdOrNull()?.link()
            ?: return CreatePostError.Unauthorized.asError()

        val replyToPost = replyTo?.resolve()

        val content = content.map()
            ?: return CreatePostError.InvalidInputContent.asError()

        val post = service.post.create(
            when (location) {
                is Global -> backend.core.types.InputPostLocation.Global
                is Profile -> {
                    val user = assembler.resolve(location.user)
                        ?:return CreatePostError.Unauthorized.asError()
                    backend.core.types.InputPostLocation.Profile(user)
                }
            },
            userLink,
            replyToPost,
            content
        )
            .successOrElse { error ->
                return when (error) {
                    DomainCreatePostError.InvalidInputContent -> CreatePostError.InvalidInputContent
                    DomainCreatePostError.InvalidReplyRef -> CreatePostError.InvalidInputReply
                    DomainCreatePostError.InvalidAuthorRef -> CreatePostError.Unauthorized
                    DomainCreatePostError.InvalidInputLocation -> error("Unreachable")
                }.asError()
            }

        return post.map().asOk()
    }

    context(_: Context)
    override suspend fun sliceFeed(
        key: SliceKey<InputFeed, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post> = context {
        val key = key.decodePayload<CursorPayload, _>(Json)
        val feed = key.fold(
            initialize = { it },
            next = { it.feed }
        )

        val serviceKey = key
            .mapCursor { it.serviceCursor }

        val serviceSlice = when (feed) {
            is InputFeed.Global -> service.post.sliceGlobal(
                key = serviceKey
                    .mapOptions {  },
                limit = limit,
            )

            is InputFeed.Profile -> service.post.sliceProfile(
                key = serviceKey
                    .mapOptions { feed.user.map() },
                limit = limit,
            ) ?: return@context Slice(emptyList(), null)
        }

        val items = coroutineScope {
            serviceSlice.items
                .map {
                    async { it.map() }
                }
                .awaitAll()
        }

        val nextPayload = if (serviceSlice.nextCursor != null) {
            CursorPayload(feed, serviceSlice.nextCursor!!)
        } else null

        Slice(
            items = items,
            nextCursor = Cursor.encodePayloadIfNotNull(Json, nextPayload)
        )
    }
}
