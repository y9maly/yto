package domain.selector

import backend.core.types.PostDescriptor
import backend.core.types.PostReference
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

        return repo.post.get(ref)?.id
    }
}
