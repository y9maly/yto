package domain.selector

import backend.core.types.UserRef
import backend.core.types.UserLink
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass


open class UserSelector @InterfaceClass constructor(
    private val repo: MainRepository,
) {
    suspend fun select(ref: UserRef): UserLink? {
        check(ref is UserLink) { TODO() }
        return ref
    }
}
