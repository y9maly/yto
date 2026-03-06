package domain.selector

import backend.core.types.PostDescriptor
import backend.core.types.PostReference
import backend.core.types.UserDescriptor
import backend.core.types.UserReference
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass


@OptIn(InterfaceClass::class)
class MainSelector(
    repo: MainRepository,
) {
    val post = PostSelector(this, repo)
    val user = UserSelector(repo)
    suspend fun select(descriptor: PostDescriptor) = post.select(descriptor)
    suspend fun select(descriptor: UserDescriptor) = user.select(descriptor)
}
