package domain.service

import backend.core.types.*
import domain.service.result.*
import integration.repository.RepositoryCollection
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.map
import kotlin.time.Clock


class UserServiceImpl(
    private val repo: RepositoryCollection,
    private val clock: Clock,
) : UserService {
    override suspend fun resolve(ref: UserRef): UserId? {
        return repo.user.resolve(ref)
    }

    override suspend fun get(id: UserId): User? {
        return repo.user.get(id)
    }

    override suspend fun exists(id: UserId): Boolean {
        return repo.user.exists(id)
    }

    override suspend fun findByPhoneNumber(phoneNumber: String): User? {
        return repo.user.getByPhoneNumber(phoneNumber)
    }

    override suspend fun findByEmail(email: String): User? {
        return repo.user.getByEmail(email)
    }

    override suspend fun register(
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
        return repo.user.create(
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

    override suspend fun edit(
        id: UserId,
        phoneNumber: Optional<String?>,
        email: Optional<String?>,
        firstName: Optional<String>,
        lastName: Optional<String?>,
        bio: Optional<String?>,
        birthday: Optional<Birthday?>,
        cover: Optional<FileId?>,
        avatar: Optional<FileId?>,
    ): EditUserResult {
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

        return repo.user.edit(
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
