package integration.repository

import backend.core.types.FileId
import backend.core.types.User
import backend.core.types.UserId
import backend.core.types.UserRef
import integration.repository.result.EditUserResult
import y9to.common.types.Birthday
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import kotlin.time.Instant


interface UserRepository {
    val firstNameLength: IntRange
    val lastNameLength: IntRange
    val bioLength: IntRange

    suspend fun resolve(ref: UserRef): UserId?
    suspend fun get(id: UserId): User?
    suspend fun exists(id: UserId): Boolean

    suspend fun getByPhoneNumber(phoneNumber: String): User?
    suspend fun getByEmail(email: String): User?

    suspend fun create(
        registrationDate: Instant,
        phoneNumber: Optional<String?>,
        email: Optional<String?>,
        firstName: String,
        lastName: Optional<String?>,
        bio: Optional<String?>,
        birthday: Optional<Birthday?>,
        cover: Optional<FileId?>,
        avatar: Optional<FileId?>,
    ): User

    /**
     * @return new user; null if invalid user id
     */
    suspend fun edit(
        id: UserId,
        phoneNumber: Optional<String?> = none(),
        email: Optional<String?> = none(),
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
        cover: Optional<FileId?> = none(),
        avatar: Optional<FileId?> = none(),
    ): EditUserResult
}
