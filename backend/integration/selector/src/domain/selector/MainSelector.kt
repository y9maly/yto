package domain.selector

import backend.core.types.PostReference
import backend.core.types.UserReference
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
