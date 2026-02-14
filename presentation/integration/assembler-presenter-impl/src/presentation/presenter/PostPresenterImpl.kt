package presentation.presenter

import domain.service.MainService
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.mapper.map
import y9to.api.types.Post


class PostPresenterImpl(
    private val service: MainService,
) : PostPresenter {
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
}
