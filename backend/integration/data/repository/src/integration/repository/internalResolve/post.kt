package integration.repository.internalResolve

import backend.core.reference.PostReference
import backend.core.types.PostId
import integration.repository.MainRepository
import y9to.libs.stdlib.successOrNull


internal suspend fun MainRepository.resolve(ref: PostReference): PostId? {
    if (ref is PostReference.Id)
        return ref.id
    return post.select(ref)?.id
}
