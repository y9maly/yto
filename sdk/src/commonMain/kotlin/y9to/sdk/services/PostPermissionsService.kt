package y9to.sdk.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import y9to.api.types.PostId
import y9to.sdk.Client
import y9to.sdk.clientId
import y9to.sdk.isAuthenticated
import y9to.sdk.types.PostPermissions


interface PostPermissionsService {
    fun getPermissions(post: PostId): Flow<PostPermissions>
}

class PostPermissionsServiceDefault(private val client: Client) : PostPermissionsService {
    override fun getPermissions(post: PostId) = combine(
        canReply(post),
        canRepost(post),
        canEdit(post),
        canDelete(post),
    ) { canReply, canRepost, canEdit, canDelete ->
        PostPermissions(
            canReply = canReply,
            canRepost = canRepost,
            canEdit = canEdit,
            canDelete = canDelete,
        )
    }

    private fun canReply(post: PostId) = client.auth.isAuthenticated

    private fun canRepost(post: PostId) = client.auth.isAuthenticated

    private fun canEdit(post: PostId) = combine(
        client.auth.clientId,
        client.post.getFlow(post),
    ) { myId, post ->
        post != null &&
                myId != null &&
                post.author.id == myId
    }

    private fun canDelete(post: PostId) = combine(
        client.auth.clientId,
        client.post.getFlow(post),
    ) { myId, post ->
        post != null &&
                myId != null &&
                post.author.id == myId
    }
}
