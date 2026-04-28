package domain.service

import backend.core.types.*
import domain.event.UserEdited
import domain.event.UserRegistered
import domain.service.result.*
import integration.eventCollector.EventCollector
import integration.repository.RepositoryCollection
import integration.repository.result.CreateUserError
import integration.repository.result.CreateUserResult
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.ifSuccess
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.map
import y9to.libs.stdlib.successOrElse
import kotlin.time.Clock


class UserServiceImpl(
    private val repo: RepositoryCollection,
    private val eventCollector: EventCollector,
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

    override suspend fun findByTelegramAuthId(telegramAuthId: String): User? {
        return repo.user.getByTelegramAuthId(telegramAuthId)
    }

    override suspend fun findByPhoneNumber(phoneNumber: String): User? {
        return repo.user.getByPhoneNumber(phoneNumber)
    }

    override suspend fun findByEmail(email: String): User? {
        return repo.user.getByEmail(email)
    }

    override suspend fun register(
        session: SessionId,
        telegramAuthId: String?,
        phoneNumber: String?,
        email: String?,
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        cover: FileId?,
        avatar: FileId?,
    ): RegisterUserResult {
        val user = repo.user.create(
            registrationDate = clock.now(),
            telegramAuthId = telegramAuthId,
            phoneNumber = phoneNumber,
            email = email,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        ).successOrElse { error ->
            return when (error) {
                CreateUserError.PhoneNumberConflict -> RegisterUserError.PhoneNumberConflict.asError()
                CreateUserError.EmailConflict -> RegisterUserError.EmailConflict.asError()
            }
        }

        eventCollector.emit(UserRegistered(user))

        return user.asOk()
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

        val result = repo.user.edit(
            id = id,
            phoneNumber = phoneNumber,
            email = email,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        )

        result.onSuccess { result ->
            eventCollector.emit(UserEdited(result.new))
        }

        return result.map()
    }
}
