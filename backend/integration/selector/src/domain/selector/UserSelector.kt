package domain.selector

import backend.core.types.UserReference
import backend.core.types.UserId
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass


open class UserSelector @InterfaceClass constructor(
    private val repo: MainRepository,
) {
    suspend fun select(ref: UserReference): UserId? {
        return repo.user.get(ref)?.id
    }
}
