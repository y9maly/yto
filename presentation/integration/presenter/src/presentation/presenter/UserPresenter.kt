@file:Suppress("RedundantSuspendModifier")

package presentation.presenter

import presentation.integration.callContext.CallContext
import y9to.api.types.User
import backend.core.types.User as BackendUser


interface UserPresenter {
    context(callContext: CallContext)
    suspend fun User(backendUser: BackendUser): User
}
