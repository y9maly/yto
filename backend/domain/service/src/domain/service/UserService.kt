package domain.service

import backend.core.reference.UserReference
import backend.core.types.FileId
import backend.core.types.SessionId
import backend.core.types.User
import backend.core.types.UserId
import domain.selector.MainSelector
import domain.service.result.*
import integration.repository.MainRepository
import y9to.common.types.Birthday
import y9to.libs.stdlib.InterfaceClass
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.map
import y9to.libs.stdlib.optional.none
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

    suspend fun exists(id: UserId) = exists(UserReference.Id(id))
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
        cover: Optional<FileId?>,
        avatar: Optional<FileId?>,
    ): User {
        return repo.user.insert(
            registrationDate = clock.now(),
            phoneNumber = phoneNumber,
            email = email,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        )
    }

    suspend fun edit(
        ref: UserReference,
        phoneNumber: Optional<String?> = none(),
        email: Optional<String?> = none(),
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
        cover: Optional<FileId?> = none(),
        avatar: Optional<FileId?> = none(),
    ): EditUserResult {
        val id = selector.select(ref)
            ?: return EditUserError.UnknownUserReference.asError()

        val firstNameError = firstName.map { firstName ->
            if (firstName.isBlank())
                return@map EditUserNameError.CannotBeBlank
            if (firstName.length !in repo.user.firstNameLength)
                return@map EditUserNameError.ExceededLengthRange
            null
        }.getOrNull()

        val lastNameError = lastName.map { lastName ->
            lastName ?: return@map null
            if (lastName.isBlank())
                return@map EditUserNameError.CannotBeBlank
            if (lastName.length !in repo.user.lastNameLength)
                return@map EditUserNameError.ExceededLengthRange
            null
        }.getOrNull()

        val bioError = bio.map { bio ->
            bio ?: return@map null
            if (bio.length !in repo.user.bioLength)
                return@map EditUserBioError.ExceededLengthRange
            null
        }.getOrNull()

        if (arrayOf(
            firstNameError,
            lastNameError,
            bioError,
        ).any { it != null }) {
            return EditUserError.FieldErrors(
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                bioError = bioError,
            ).asError()
        }

        return repo.user.update(
            id = id,
            phoneNumber = phoneNumber,
            email = email,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        ).map()
    }
}
