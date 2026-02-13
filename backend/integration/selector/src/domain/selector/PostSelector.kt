package domain.selector

import backend.core.reference.PostDescriptor
import backend.core.reference.PostReference
import backend.core.types.PostId
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass
import y9to.libs.stdlib.successOrElse


open class PostSelector @InterfaceClass constructor(
    private val main: MainSelector,
    private val repo: MainRepository,
) {
    suspend fun select(ref: PostReference): PostId? {
        if (ref is PostReference.Id)
            return ref.id

        when (ref) {
            PostReference.First ->
                return repo.post.selectFirstPost()?.id
            PostReference.Random ->
                return repo.post.selectRandomPost()?.id
            PostReference.Last ->
                return repo.post.selectLastPost()?.id
            else -> {}
        }

        val userRef = when (ref) {
            is PostReference.FirstOfAuthor -> ref.author
            is PostReference.LastOfAuthor -> ref.author
            is PostReference.RandomOfAuthor -> ref.author
            is PostDescriptor.FirstOfAuthor -> TODO()
            is PostDescriptor.LastOfAuthor -> TODO()
            is PostDescriptor.RandomOfAuthor -> TODO()
        }

        val userId = main.user.select(userRef)
            ?: return null

        val result = when (ref) {
            is PostReference.FirstOfAuthor ->
                repo.post.selectFirstPost(userId)

            is PostReference.LastOfAuthor ->
                repo.post.selectLastPost(userId)

            is PostReference.RandomOfAuthor ->
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
