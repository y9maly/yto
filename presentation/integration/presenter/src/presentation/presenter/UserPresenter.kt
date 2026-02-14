@file:Suppress("RedundantSuspendModifier")

package presentation.presenter

import presentation.integration.context.Context
import y9to.api.types.MyProfile
import y9to.api.types.User
import backend.core.types.User as BackendUser


interface UserPresenter {
    context(context: Context)
    suspend fun User(backendUser: BackendUser): User

    context(context: Context)
    suspend fun MyProfile(backendUser: BackendUser): MyProfile
}


context(_: Context, presenter: UserPresenter)
suspend fun BackendUser.mapAsUser(): User = presenter.User(this)

context(_: Context, presenter: UserPresenter)
suspend fun BackendUser.mapAsMyProfile(): MyProfile = presenter.MyProfile(this)
