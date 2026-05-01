package y9to.sdk.services

import y9to.api.types.EditPostResult
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import y9to.sdk.Client


interface EditPostService {
    suspend fun edit(
        post: InputPost,
        replyTo: Optional<InputPost?> = none(),
        content: Optional<InputPostContent> = none(),
    ): EditPostResult
}


class EditPostServiceDefault(private val client: Client) : EditPostService {
    override suspend fun edit(
        post: InputPost,
        replyTo: Optional<InputPost?>,
        content: Optional<InputPostContent>
    ): EditPostResult {
        return client.post.edit(
            post = post,
            replyTo = replyTo,
            content = content,
        )
    }
}
