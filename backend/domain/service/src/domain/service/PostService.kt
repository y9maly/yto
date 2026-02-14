package domain.service

import backend.core.types.InputPostContent
import backend.core.types.InputPostLocation
import backend.core.types.Post
import backend.core.types.PostId
import backend.core.types.PostLocationPredicate
import backend.core.types.PostPredicate
import backend.core.types.PostReference
import backend.core.types.UserId
import backend.core.types.UserPredicate
import backend.core.types.UserReference
import backend.core.types.acceptOnly
import domain.selector.MainSelector
import domain.service.result.CreatePostError
import domain.service.result.CreatePostResult
import domain.service.result.map
import integration.repository.MainRepository
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey
import y9to.libs.paging.mapOptions
import y9to.libs.stdlib.InterfaceClass
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
        return repo.post.get(id)
    }

    suspend fun create(
        location: InputPostLocation,
        author: UserReference,
        replyTo: PostReference?,
        content: InputPostContent,
    ): CreatePostResult {
        val inputContent = content.map(selector)
            ?: return CreatePostError.InvalidInputContent.asError()
        val inputLocation = location.map(selector)
            ?: return CreatePostError.InvalidInputLocation.asError()

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
            content = inputContent,
            location = inputLocation,
        ).map()
    }

    suspend fun sliceGlobal(
        key: SliceKey<Unit>,
        limit: Int,
    ): Slice<Post> {
        return repo.post.slice(
            key = key.mapOptions {
                acceptOnly(PostPredicate.Location(
                    location = acceptOnly(PostLocationPredicate.Global)
                ))
            },
            limit = limit,
        )
    }

    suspend fun sliceProfile(
        key: SliceKey<UserId>,
        limit: Int,
    ): Slice<Post>? {
        return repo.post.slice(
            key = key.mapOptions { userId ->
                acceptOnly(PostPredicate.Location(
                    location = acceptOnly(PostLocationPredicate.Profile(
                        user = acceptOnly(UserPredicate.Id(userId))
                    ))
                ))
            },
            limit = limit,
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

private suspend fun InputPostLocation.map(
    selector: MainSelector,
): integration.repository.input.InputPostLocation? {
    return when (this) {
        is InputPostLocation.Global -> {
            integration.repository.input.InputPostLocation.Global
        }

        is InputPostLocation.Profile -> {
            val userId = selector.select(user)
                ?: return null
            integration.repository.input.InputPostLocation.Profile(userId)
        }
    }
}
