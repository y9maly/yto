package presentation.presenter

import domain.service.MainService
import presentation.integration.callContext.CallContext
import presentation.mapper.map
import y9to.api.types.Post


class PostPresenterImpl(
    private val service: MainService,
) : PostPresenter {
    context(callContext: CallContext)
    override suspend fun Post(backendPost: backend.core.types.Post): Post {
        return backendPost.map()
    }
}
