package domain.selector

import backend.core.types.PostRef
import backend.core.types.PostLink
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass


open class PostSelector @InterfaceClass constructor(
    private val main: MainSelector,
    private val repo: MainRepository,
) {
    suspend fun select(ref: PostRef): PostLink? {
        check(ref is PostLink) { TODO() }
        return ref
    }
}
