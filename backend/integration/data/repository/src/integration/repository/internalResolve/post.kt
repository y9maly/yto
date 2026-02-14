package integration.repository.internalResolve

import backend.core.types.PostReference
import backend.core.types.PostId
import integration.repository.MainRepository


internal suspend fun MainRepository.resolve(ref: PostReference): PostId? {
    if (ref is PostReference.Id)
        return ref.id
    return post.get(ref)?.id
}
