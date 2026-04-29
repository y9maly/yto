package domain.service

import backend.core.types.*
import domain.event.PostCreated
import domain.event.PostDeleted
import domain.service.result.CreatePostResult
import domain.service.result.DeletePostResult
import domain.service.result.map
import integration.eventCollector.EventCollector
import integration.repository.RepositoryCollection
import integration.repository.PostRepository
import y9to.libs.paging.*
import kotlin.time.Clock


class PostServiceImpl(
    private val repo: RepositoryCollection,
    private val eventCollector: EventCollector,
    private val clock: Clock,
) : PostService {
    override suspend fun resolve(ref: PostRef): PostId? {
        return repo.post.resolve(ref)
    }

    override suspend fun get(id: PostId): Post? {
        return repo.post.get(id)
    }

    override suspend fun create(
        location: InputPostLocation,
        author: UserId,
        replyTo: PostId?,
        content: InputPostContent,
    ): CreatePostResult {
        val inputContent = content.map()
        val inputLocation = location.map()

        val result = repo.post.create(
            creationDate = clock.now(),
            author = author,
            replyTo = replyTo,
            content = inputContent,
            location = inputLocation,
        )

        result.onSuccess { post ->
            eventCollector.emit(PostCreated(post))
        }

        return result.map()
    }

    override suspend fun delete(post: PostId): DeletePostResult {
        val result = repo.post.delete(post)

        result.onSuccess {
            eventCollector.emit(PostDeleted(post))
        }

        return result.map()
    }

    override suspend fun sliceGlobal(
        key: SliceKey<Unit, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post> {
        return repo.post.slice(
            key = key
                .mapOptions {
                    PostRepository.SliceOptions(
                        predicate = Predicate.Criteria(PostCriteria.Location(
                            location = Predicate.Criteria(PostLocationCriteria.Global)
                        ))
                    )
                },
            limit = limit,
        )
    }

    override suspend fun sliceProfile(
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
                        predicate = Predicate.Criteria(PostCriteria.Location(
                            location = Predicate.Criteria(PostLocationCriteria.Profile(
                                user = Predicate.Criteria(UserCriteria.Id(userId))
                            ))
                        ))
                    )
                },
            limit = limit,
        )
    }
}

private fun InputPostContent.map(): integration.repository.input.InputPostContent {
    return when (this) {
        is InputPostContent.Standalone -> {
            integration.repository.input.InputPostContent.Standalone(text)
        }

        is InputPostContent.Repost -> {
            integration.repository.input.InputPostContent.Repost(comment, original)
        }
    }
}

private fun InputPostLocation.map(): integration.repository.input.InputPostLocation {
    return when (this) {
        is InputPostLocation.Global -> {
            integration.repository.input.InputPostLocation.Global
        }

        is InputPostLocation.Profile -> {
            integration.repository.input.InputPostLocation.Profile(user)
        }
    }
}
