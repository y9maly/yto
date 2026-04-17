package domain.service

import backend.core.types.*
import domain.service.result.EditUserResult
import y9to.common.types.Birthday
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none


interface UserService {
    suspend fun resolve(ref: UserRef): UserId?
    suspend fun get(id: UserId): User?
    suspend fun exists(id: UserId): Boolean

    suspend fun findByPhoneNumber(phoneNumber: String): User?
    suspend fun findByEmail(email: String): User?

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
    ): User

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
