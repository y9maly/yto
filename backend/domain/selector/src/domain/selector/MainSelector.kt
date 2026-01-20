package domain.selector

import backend.core.reference.PostReference
import backend.core.reference.UserReference
import y9to.libs.stdlib.InterfaceClass


@OptIn(InterfaceClass::class)
data class MainSelector(
    val post: PostSelector,
    val user: UserSelector,
) {
    suspend fun select(ref: PostReference) = post.select(ref)
    suspend fun select(ref: UserReference) = user.select(ref)
}
