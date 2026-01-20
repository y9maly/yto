package domain.selector

import backend.core.reference.PostReference
import backend.core.types.PostId
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass
import y9to.libs.stdlib.successOrElse


open class PostSelector @InterfaceClass constructor(
    private val repo: MainRepository,
) {
    suspend fun select(ref: PostReference): PostId? {
        if (ref is PostReference.Id)
            return ref.id

        if (ref is PostReference.Random)
            return repo.post.selectRandomPost()?.id

        val userId = when (ref) {
            is PostReference.FirstPost -> ref.self
            is PostReference.LastPost -> ref.self
            is PostReference.RandomAuthor -> ref.self
        }

        val result = when (ref) {
            is PostReference.FirstPost ->
                repo.post.selectFirstPost(userId)

            is PostReference.LastPost ->
                repo.post.selectLastPost(userId)

            is PostReference.RandomAuthor ->
                repo.post.selectRandomAuthorPost(userId)
        }

        val post = result.successOrElse {
            return null
        }

        check(post.author.id == userId) {
            "User $userId is not an author of post ${post.id}. User ${post.author.id} is author."
        }

        return post.id
    }
}
