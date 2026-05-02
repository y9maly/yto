package y9to.sdk.services

import y9to.api.types.DeletePostResult
import y9to.api.types.InputPost
import y9to.sdk.Client


interface DeletePostService {
    suspend fun delete(post: InputPost): DeletePostResult
}


class DeletePostServiceDefault(private val client: Client) : DeletePostService {
    override suspend fun delete(post: InputPost): DeletePostResult {
        return client.post.delete(post)
    }
}
