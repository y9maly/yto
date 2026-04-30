package y9to.sdk.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import y9to.api.types.InputPost
import y9to.api.types.Post
import y9to.api.types.PostId
import y9to.sdk.Client


interface GetPostService {
    fun getFlow(input: InputPost): Flow<Post?>

    suspend fun get(input: InputPost) = getFlow(input).first()
}

class GetPostServiceDefault(private val client: Client) : GetPostService {
    override fun getFlow(input: InputPost) = client.post.getFlow(input)

    override suspend fun get(input: InputPost) = client.post.get(input)
}
