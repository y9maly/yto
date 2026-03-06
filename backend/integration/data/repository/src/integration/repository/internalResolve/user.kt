package integration.repository.internalResolve

import backend.core.types.UserLink
import backend.core.types.UserId
import integration.repository.MainRepository


internal suspend fun MainRepository.resolve(link: UserLink): UserId? {
    if (link is UserLink.Id)
        return link.id
    return user.get(link)?.id
}
