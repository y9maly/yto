package integration.repository

import backend.core.types.FileId
import backend.core.types.User
import backend.core.types.UserId
import backend.core.types.UserRef
import backend.infra.postgres.table.TUser
import integration.repository.internals.FirstRevision
import integration.repository.result.CreateUserError
import integration.repository.result.CreateUserResult
import integration.repository.result.EditUserError
import integration.repository.result.EditUserOk
import integration.repository.result.EditUserResult
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.onPresent
import kotlin.time.Instant


internal class PostgresUserRepository(private val main: MainRepository) : UserRepository {
    override val firstNameLength = 1..64
    override val lastNameLength = 1..64
    override val bioLength = 1..1024

    override suspend fun resolve(ref: UserRef): UserId? {
        when (ref) {
            is UserId -> return ref
        }
    }

    override suspend fun get(id: UserId): User? {
        return selectByPredicate { TUser.id eq id.long }
    }

    override suspend fun getByTelegramAuthId(telegramAuthId: String): User? {
        return selectByPredicate { TUser.telegram_auth_id eq telegramAuthId }
    }

    override suspend fun getByPhoneNumber(phoneNumber: String): User? {
        return selectByPredicate { TUser.phone_number eq phoneNumber }
    }

    override suspend fun getByEmail(email: String): User? {
        return selectByPredicate { TUser.email eq email }
    }

    private suspend inline fun selectByPredicate(noinline predicate: () -> Op<Boolean>): User? = main.transaction(ReadOnly) {
        val row = TUser.selectAll()
            .where(predicate)
            .limit(1)
            .firstOrNull()
            ?: return@transaction null
        fromRow(row)
    }

    override suspend fun exists(id: UserId): Boolean = main.transaction(ReadOnly) {
        val query = TUser
            .select(intLiteral(1))
            .where { TUser.id eq id.long }
            .limit(1)

        query.count() > 0
    }

    override suspend fun create(
        registrationDate: Instant,
        telegramAuthId: String?,
        phoneNumber: String?,
        email: String?,
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        cover: FileId?,
        avatar: FileId?,
    ): CreateUserResult = main.transaction {
        if (phoneNumber != null && getByPhoneNumber(phoneNumber) != null)
            return@transaction CreateUserError.PhoneNumberConflict.asError()

        if (email != null && getByEmail(email) != null)
            return@transaction CreateUserError.EmailConflict.asError()

        val userId = TUser.insertAndGetId { row ->
            row[this.registration_date] = registrationDate
            row[this.telegram_auth_id] = telegramAuthId
            row[this.phone_number] = phoneNumber
            row[this.email] = email
            row[this.first_name] = firstName
            row[this.last_name] = lastName
            row[this.bio] = bio
            row[this.birthday] = birthday
            row[this.cover] = cover?.long
            row[this.avatar] = avatar?.long
        }.value

        User(
            id = UserId(userId),
            revision = FirstRevision,
            registrationDate = registrationDate,
            phoneNumber = phoneNumber,
            email = email,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        ).asOk()
    }

    /**
     * @return new user; null if invalid user id
     */
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
    ): EditUserResult = main.transaction {
        cover.onPresent { cover ->
            cover ?: return@onPresent
            if (!main.file.exists(cover))
                return@transaction EditUserError.InvalidCoverFileId.asError()
        }

        avatar.onPresent { avatar ->
            avatar ?: return@onPresent
            if (!main.file.exists(avatar))
                return@transaction EditUserError.InvalidAvatarFileId.asError()
        }

        TUser.update(where = { TUser.id eq id.long }) { row ->
            phoneNumber.onPresent {
                row[TUser.phone_number] = it
            }

            email.onPresent {
                row[TUser.email] = it
            }

            firstName.onPresent {
                row[TUser.first_name] = it
            }

            lastName.onPresent {
                row[TUser.last_name] = it
            }

            bio.onPresent {
                row[TUser.bio] = it
            }

            birthday.onPresent {
                row[TUser.birthday] = it
            }

            cover.onPresent {
                row[TUser.cover] = it?.long
            }

            avatar.onPresent {
                row[TUser.avatar] = it?.long
            }
        }

        val newUser = get(id)
            ?: return@transaction EditUserError.InvalidUserLink.asError()

        EditUserOk(
            new = newUser,
        ).asOk()
    }
}

private fun fromRow(row: ResultRow) = User(
    id = UserId(row[TUser.id].value),
    revision = row[TUser.revision],
    registrationDate = row[TUser.registration_date],
    phoneNumber = row[TUser.phone_number],
    email = row[TUser.email],
    firstName = row[TUser.first_name],
    lastName = row[TUser.last_name],
    bio = row[TUser.bio],
    birthday = row[TUser.birthday],
    cover = row[TUser.cover]?.let(::FileId),
    avatar = row[TUser.avatar]?.let(::FileId),
)
