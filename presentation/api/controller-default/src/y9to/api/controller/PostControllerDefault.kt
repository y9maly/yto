package y9to.api.controller

import domain.service.ServiceCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import presentation.assembler.AssemblerCollection
import presentation.assembler.map
import presentation.assembler.resolve
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.presenter.PresenterCollection
import presentation.presenter.map
import y9to.api.types.*
import y9to.libs.paging.*
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.map
import y9to.libs.stdlib.successOrElse
import domain.service.result.CreatePostError as DomainCreatePostError


class PostControllerDefault(
    private val service: ServiceCollection,
    override val assembler: AssemblerCollection,
    override val presenter: PresenterCollection,
) : PostController, ControllerDefault {
    companion object {
        val logger = KotlinLogging.logger { }
    }

    @Serializable
    private data class CursorPayload(
        val feed: InputFeed,
        val serviceCursor: Cursor,
    )

    context(_: Context)
    override suspend fun resolve(input: InputPost): PostId? = context {
        return input.resolve()?.map()
    }

    context(_: Context)
    override suspend fun get(input: InputPost): Post? = context {
        val postId = input.resolve() ?: return null
        val post = service.post.get(postId) ?: return null
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

        val userId = authState.userIdOrNull()
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
            author = userId,
            replyTo = replyToPost,
            content
        )
            .successOrElse { error ->
                return when (error) {
                    DomainCreatePostError.InvalidInputContent -> CreatePostError.InvalidInputContent
                    DomainCreatePostError.InvalidReplyTo -> CreatePostError.InvalidInputReplyTo
                    DomainCreatePostError.InvalidAuthorId -> CreatePostError.Unauthorized
                    DomainCreatePostError.InvalidInputLocation -> error("Unreachable")
                }.asError()
            }

        return post.map().asOk()
    }

    context(_: Context)
    override suspend fun edit(
        post: InputPost,
        replyTo: Optional<InputPost?>,
        content: Optional<InputPostContent>
    ): EditPostResult = context {
        val authState = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: return EditPostError.Unauthorized.asError()
        }

        val userId = authState.userIdOrNull()
            ?: return EditPostError.Unauthorized.asError()

        val postId = post.resolve()
            ?: return EditPostError.InvalidInputPost.asError()
        val post = service.post.get(postId)
            ?: return EditPostError.InvalidInputPost.asError()

        if (post.author.id != userId)
            return EditPostError.AccessDenied.asError()

        val replyTo = replyTo.map { it?.resolve() }
        val content = content.map { it.map() ?: return EditPostError.InvalidNewInputContent.asError() }

        val result = service.post.edit(
            post = postId,
            replyTo = replyTo,
            content = content,
        ).successOrElse { error ->
            return when (error) {
                domain.service.result.EditPostError.InvalidPostId ->
                    EditPostError.InvalidInputPost

                domain.service.result.EditPostError.InvalidNewInputContent ->
                    EditPostError.InvalidNewInputContent

                domain.service.result.EditPostError.InvalidNewReplyTo ->
                    EditPostError.InvalidNewInputReplyTo

                domain.service.result.EditPostError.InvalidNewAuthorId -> {
                    val message = "Must be unreachable because we are not trying to change the author here"
                    logger.error { message }
                    error(message)
                }
            }.asError()
        }

        result.map().asOk()
    }

    context(_: Context)
    override suspend fun delete(post: InputPost): DeletePostResult = context {
        val authState = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: return DeletePostError.Unauthorized.asError()
        }

        val userId = authState.userIdOrNull()
            ?: return DeletePostError.Unauthorized.asError()

        val postId = post.resolve()
            ?: return DeletePostError.InvalidInputPost.asError()
        val post = service.post.get(postId)
            ?: return DeletePostError.InvalidInputPost.asError()

        if (post.author.id != userId)
            return DeletePostError.AccessDenied.asError()

        service.post.delete(postId)
            .successOrElse { error ->
                return when (error) {
                    is domain.service.result.DeletePostError.InvalidPostId ->
                        DeletePostError.InvalidInputPost
                }.asError()
            }

        Unit.asOk()
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
