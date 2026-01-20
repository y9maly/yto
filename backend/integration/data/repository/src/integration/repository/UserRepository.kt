package integration.repository

import integration.repository.internals.FirstRevision
import integration.repository.result.UpdateUserError
import integration.repository.result.UpdateUserOk
import integration.repository.result.UpdateUserResult
import backend.infra.postgres.table.TUser
import backend.core.types.User
import backend.core.types.UserId
import backend.infra.postgres.view.VPost
import integration.repository.internals.RandomFunction
import integration.repository.result.SelectAuthorPostError
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
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
import y9to.libs.stdlib.optional.none
import y9to.libs.stdlib.optional.onPresent
import kotlin.time.Instant


class UserRepository internal constructor(private val main: MainRepository) {
    suspend fun select(id: UserId): User? {
        return selectByPredicate { TUser.id eq id.long }
    }

    suspend fun selectRandom(): User? = main.transaction(ReadOnly) {
        val row = TUser.selectAll()
            .orderBy(RandomFunction() to SortOrder.ASC)
            .limit(1)
            .firstOrNull()
            ?: return@transaction null
        fromRow(row)
    }

    suspend fun selectByPhoneNumber(phoneNumber: String): User? {
        return selectByPredicate { TUser.phone_number eq phoneNumber }
    }

    suspend fun selectByEmail(email: String): User? {
        return selectByPredicate { TUser.email eq email }
    }

    private suspend inline fun selectByPredicate(noinline predicate: () -> Op<Boolean>): User? = main.transaction(
        ReadOnly
    ) {
        val row = TUser.selectAll()
            .where(predicate)
            .limit(1)
            .firstOrNull()
            ?: return@transaction null
        fromRow(row)
    }

    suspend fun exists(user: UserId): Boolean = main.transaction(ReadOnly) {
        TUser
            .select(intLiteral(1))
            .where { TUser.id eq user.long }
            .limit(1)
            .count() > 0
    }

    suspend fun insert(
        registrationDate: Instant,
        phoneNumber: Optional<String?>,
        email: Optional<String?>,
        firstName: String,
        lastName: Optional<String?>,
        bio: Optional<String?>,
        birthday: Optional<Birthday?>,
    ): User = main.transaction {
        val userId = TUser.insertAndGetId { row ->
            row[this.registration_date] = registrationDate
            row[this.phone_number] = phoneNumber.getOrNull()
            row[this.email] = email.getOrNull()
            row[this.first_name] = firstName
            row[this.last_name] = lastName.getOrNull()
            row[this.bio] = bio.getOrNull()
            row[this.birthday] = birthday.getOrNull()
        }.value

        User(
            id = UserId(userId),
            revision = FirstRevision,
            registrationDate = registrationDate,
            phoneNumber = phoneNumber.getOrNull(),
            email = email.getOrNull(),
            firstName = firstName,
            lastName = lastName.getOrNull(),
            bio = bio.getOrNull(),
            birthday = birthday.getOrNull(),
        ).also {
//            main.eventsCollector.onEvent(UserInserted(it))
        }
    }

    /**
     * @return new user; null if invalid user id
     */
    suspend fun update(
        id: UserId,
        phoneNumber: Optional<String?> = none(),
        email: Optional<String?> = none(),
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
    ): UpdateUserResult = main.transaction {
        val oldUser = select(id)
            ?: return@transaction UpdateUserError.UnknownUserId.asError()

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
        }

        val newUser = select(id)
            ?: return@transaction UpdateUserError.UnknownUserId.asError()

//        main.eventsCollector.onEvent(UserUpdated(old = oldUser, new = newUser))

        UpdateUserOk(
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
)
