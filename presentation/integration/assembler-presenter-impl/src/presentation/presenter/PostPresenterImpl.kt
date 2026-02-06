package presentation.presenter

import domain.service.MainService
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.authStateOrPut
import presentation.integration.callContext.elements.sessionId
import presentation.mapper.map
import y9to.api.types.Post


class PostPresenterImpl(
    private val service: MainService,
) : PostPresenter {
    context(callContext: CallContext)
    override suspend fun Post(backendPost: backend.core.types.Post): Post {
        val userId = callContext.authStateOrPut {
            service.auth.getAuthState(callContext.sessionId)
                ?: error("Invalid session id ${callContext.sessionId}")
        }.userIdOrNull()

        val isAuthor = userId != null && userId == backendPost.author.id

        return backendPost.map(
            canEdit = isAuthor,
            canDelete = isAuthor,
        )
    }
}
