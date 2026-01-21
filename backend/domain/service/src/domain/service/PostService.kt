package domain.service

import backend.core.input.InputPostContent
import backend.core.reference.PostReference
import backend.core.reference.UserReference
import backend.core.types.Post
import backend.core.types.PostId
import domain.selector.MainSelector
import domain.service.result.CreatePostError
import domain.service.result.CreatePostResult
import domain.service.result.map
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass
import y9to.libs.stdlib.Slice
import y9to.libs.stdlib.SpliceKey
import y9to.libs.stdlib.asError
import kotlin.time.Clock


class PostService @InterfaceClass constructor(
    private val repo: MainRepository,
    private val selector: MainSelector,
    private val clock: Clock,
) {
    suspend fun get(id: PostId) = get(PostReference.Id(id))
    suspend fun get(ref: PostReference): Post? {
        val id = selector.select(ref) ?: return null
        return repo.post.select(id)
    }

    suspend fun create(
        author: UserReference,
        replyTo: PostReference?,
        content: InputPostContent,
    ): CreatePostResult {
        val inputContent = content.map(selector)
            ?: return CreatePostError.InvalidInputContent.asError()

        val authorId = selector.select(author)
            ?: return CreatePostError.UnknownAuthorReference.asError()
        val replyToId = replyTo?.let {
            selector.select(replyTo)
                ?: return CreatePostError.UnknownReplyToPostReference.asError()
        }

        return repo.post.insert(
            creationDate = clock.now(),
            author = authorId,
            replyTo = replyToId,
            content = inputContent
        ).map()
    }

    suspend fun sliceGlobal(
        key: SpliceKey<Unit>,
        limit: Int,
    ): Slice<Post> {
        return repo.post.sliceGlobal(
            key = key,
            limit = limit
        )
    }
}

// todo assemble layer?
private suspend fun InputPostContent.map(
    selector: MainSelector,
): integration.repository.input.InputPostContent? {
    return when (this) {
        is InputPostContent.Standalone -> {
            integration.repository.input.InputPostContent.Standalone(text)
        }

        is InputPostContent.Repost -> {
            val original = selector.select(original)
                ?: return null
            integration.repository.input.InputPostContent.Repost(comment, original)
        }
    }
}
