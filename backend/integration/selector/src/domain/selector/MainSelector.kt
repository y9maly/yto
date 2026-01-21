package domain.selector

import backend.core.reference.PostReference
import backend.core.reference.UserReference
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass


@OptIn(InterfaceClass::class)
class MainSelector(
    repo: MainRepository,
) {
    val post = PostSelector(this, repo)
    val user = UserSelector(repo)
    suspend fun select(ref: PostReference) = post.select(ref)
    suspend fun select(ref: UserReference) = user.select(ref)
}
