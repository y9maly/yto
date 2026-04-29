@file:Suppress("RedundantSuspendModifier")

package presentation.presenter

import presentation.integration.context.Context
import y9to.api.types.Post
import y9to.api.types.PostContent
import y9to.api.types.PostId
import backend.core.types.Post as BackendPost
import backend.core.types.PostId as BackendPostId
import backend.core.types.PostContent as BackendPostContent


interface PostPresenter {
    context(context: Context)
    suspend fun PostId(backendPostId: BackendPostId): PostId

    context(context: Context)
    suspend fun Post(backendPost: BackendPost): Post

    context(context: Context)
    suspend fun PostContent(backendPost: BackendPostContent): PostContent
}


context(_: Context, presenter: PostPresenter)
suspend fun BackendPostId.map(): PostId = presenter.PostId(this)

context(_: Context, presenter: PostPresenter)
suspend fun BackendPost.map(): Post = presenter.Post(this)

context(_: Context, presenter: PostPresenter)
suspend fun BackendPostContent.map(): PostContent = presenter.PostContent(this)
