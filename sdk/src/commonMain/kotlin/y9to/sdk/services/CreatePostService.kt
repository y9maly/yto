package y9to.sdk.services

import y9to.api.types.CreatePostResult
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.InputPostLocation
import y9to.sdk.Client


interface CreatePostService {
    suspend fun create(
        location: InputPostLocation,
        replyTo: InputPost? = null,
        content: InputPostContent,
    ): CreatePostResult
}

class CreatePostServiceDefault(private val client: Client) : CreatePostService {
    override suspend fun create(
        location: InputPostLocation,
        replyTo: InputPost?,
        content: InputPostContent
    ): CreatePostResult {
        return client.post.create(location, replyTo, content)
    }
}
