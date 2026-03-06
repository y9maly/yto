package domain.selector

import backend.core.types.PostRef
import backend.core.types.UserRef
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass


@OptIn(InterfaceClass::class)
class MainSelector(
    repo: MainRepository,
) {
    val post = PostSelector(this, repo)
    val user = UserSelector(repo)
    suspend fun select(ref: PostRef) = post.select(ref)
    suspend fun select(ref: UserRef) = user.select(ref)
}
