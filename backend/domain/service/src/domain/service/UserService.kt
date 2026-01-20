package domain.service

import backend.core.reference.UserReference
import backend.core.types.SessionId
import backend.core.types.User
import backend.core.types.UserId
import domain.selector.MainSelector
import domain.service.result.EditUserError
import domain.service.result.EditUserResult
import domain.service.result.map
import integration.repository.MainRepository
import y9to.common.types.Birthday
import y9to.libs.stdlib.InterfaceClass
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.optional.Optional
import kotlin.time.Clock


class UserService @InterfaceClass constructor(
    private val repo: MainRepository,
    private val selector: MainSelector,
    private val clock: Clock,
) {
    suspend fun get(id: UserId) = get(UserReference.Id(id))
    suspend fun get(ref: UserReference): User? {
        val id = selector.select(ref) ?: return null
        return repo.user.select(id)
    }

    suspend fun exists(ref: UserReference): Boolean {
        val id = selector.select(ref) ?: return false
        return repo.user.exists(id)
    }

    suspend fun findByPhoneNumber(phoneNumber: String): User? {
        return repo.user.selectByPhoneNumber(phoneNumber)
    }

    suspend fun findByEmail(email: String): User? {
        return repo.user.selectByEmail(email)
    }

    suspend fun register(
        session: SessionId,
        firstName: String,
        lastName: Optional<String>,
        email: Optional<String>,
        phoneNumber: Optional<String>,
        bio: Optional<String>,
        birthday: Optional<Birthday>,
    ): User {
        return repo.user.insert(
            registrationDate = clock.now(),
            phoneNumber = phoneNumber,
            email = email,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
        )
    }

    suspend fun edit(
        ref: UserReference,
        phoneNumber: Optional<String?>,
        email: Optional<String?>,
        firstName: Optional<String>,
        lastName: Optional<String?>,
        bio: Optional<String?>,
    ): EditUserResult {
        val id = selector.select(ref)
            ?: return EditUserError.UnknownUserReference.asError()

        return repo.user.update(
            id = id,
            phoneNumber = phoneNumber,
            email = email,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
        ).map()
    }
}
