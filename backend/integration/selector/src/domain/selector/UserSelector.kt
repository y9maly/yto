package domain.selector

import backend.core.reference.UserReference
import backend.core.types.UserId
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass


open class UserSelector @InterfaceClass constructor(
    private val repo: MainRepository,
) {
    suspend fun select(ref: UserReference): UserId? = when (ref) {
        is UserReference.Id -> {
            return ref.id
        }

        is UserReference.Random -> {
            return repo.user.selectRandom()?.id
        }
    }
}
