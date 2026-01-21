@file:Suppress("RedundantSuspendModifier")

package presentation.presenter

import presentation.integration.callContext.CallContext
import y9to.api.types.Post
import backend.core.types.Post as BackendPost


interface PostPresenter {
    context(callContext: CallContext)
    suspend fun Post(backendPost: BackendPost): Post
}
