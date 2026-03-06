package domain.service

import backend.core.types.*
import domain.selector.MainSelector
import domain.service.result.CreatePostError
import domain.service.result.CreatePostResult
import domain.service.result.DeletePostError
import domain.service.result.map
import integration.repository.MainRepository
import integration.repository.PostRepository
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import y9to.libs.paging.*
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

    suspend fun delete(post: PostReference): DeletePostError {
        TODO()
    }

    suspend fun sliceGlobal(
        key: SliceKey<Unit, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post> {
        return repo.post.slice(
            key = key
                .mapOptions {
                    PostRepository.SliceOptions(
                        filter = acceptOnly(PostPredicate.Location(
                            location = acceptOnly(PostLocationPredicate.Global)
                        ))
                    )
                },
            limit = limit,
        )
    }

    suspend fun sliceProfile(
        key: SliceKey<UserId, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post>? {
        key.onInitialize { userId ->
            if (!repo.user.exists(userId)) {
                return null
            }
        }

        return repo.post.slice(
            key = key
                .mapOptions { userId ->
                    PostRepository.SliceOptions(
                        filter = acceptOnly(PostPredicate.Location(
                            location = acceptOnly(PostLocationPredicate.Profile(
                                user = acceptOnly(UserPredicate.Id(userId))
                            ))
                        ))
                    )
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
            val originalRef = selector.select(original)
                ?: return null
            integration.repository.input.InputPostContent.Repost(comment, originalRef)
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
            val userRef = selector.select(user)
                ?: return null
            integration.repository.input.InputPostLocation.Profile(userRef)
        }
    }
}
