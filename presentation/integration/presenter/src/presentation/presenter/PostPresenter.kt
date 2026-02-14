@file:Suppress("RedundantSuspendModifier")

package presentation.presenter

import presentation.integration.context.Context
import y9to.api.types.Post
import backend.core.types.Post as BackendPost


interface PostPresenter {
    context(context: Context)
    suspend fun Post(backendPost: BackendPost): Post
}


context(_: Context, presenter: PostPresenter)
suspend fun BackendPost.map(): Post = presenter.Post(this)
