package integration.repository.internalResolve

import backend.core.types.UserReference
import backend.core.types.UserId
import integration.repository.MainRepository


internal suspend fun MainRepository.resolve(ref: UserReference): UserId? {
    if (ref is UserReference.Id)
        return ref.id
    return user.get(ref)?.id
}
