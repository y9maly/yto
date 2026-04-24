package presentation.presenter

import domain.service.ServiceCollection
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.mapper.map
import y9to.api.types.Post
import y9to.api.types.PostContent
import y9to.api.types.PostId


class PostPresenterImpl(
    private val service: ServiceCollection,
) : PostPresenter {
    context(context: Context)
    override suspend fun PostId(backendPostId: backend.core.types.PostId): PostId {
        return backendPostId.map()
    }

    context(context: Context)
    override suspend fun Post(backendPost: backend.core.types.Post): Post {
        val userId = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: error("Invalid session id $sessionId")
        }.userIdOrNull()

        val isAuthor = userId != null && userId == backendPost.author.id

        return backendPost.map(
            canEdit = isAuthor,
            canDelete = isAuthor,
        )
    }

    context(context: Context)
    override suspend fun PostContent(backendPost: backend.core.types.PostContent): PostContent {
        return backendPost.map()
    }
}
