package integration.repository.internalResolve

import backend.core.types.PostLink
import backend.core.types.PostId
import integration.repository.MainRepository


internal suspend fun MainRepository.resolve(link: PostLink): PostId? {
    if (link is PostLink.Id)
        return link.id
    return post.get(link)?.id
}
