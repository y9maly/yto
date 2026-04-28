package domain.service

import backend.core.types.*
import domain.service.result.EditUserResult
import domain.service.result.RegisterUserResult
import y9to.common.types.Birthday
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import kotlin.time.Instant


interface UserService {
    suspend fun resolve(ref: UserRef): UserId?
    suspend fun get(id: UserId): User?
    suspend fun exists(id: UserId): Boolean

    suspend fun findByTelegramAuthId(telegramAuthId: String): User?
    suspend fun findByPhoneNumber(phoneNumber: String): User?
    suspend fun findByEmail(email: String): User?

    suspend fun register(
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
    ): RegisterUserResult

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
