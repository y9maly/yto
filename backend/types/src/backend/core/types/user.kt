package backend.core.types

import kotlinx.serialization.Serializable as S
import y9to.common.types.Birthday
import kotlin.time.Instant


@JvmInline
@S value class UserId(val long: Long) : ClientId

@S data class User(
    val id: UserId,
    val revision: Revision,
    val registrationDate: Instant,
    val firstName: String,
    val lastName: String?,
    val phoneNumber: String?,
    val email: String?,
    val bio: String?,
    val birthday: Birthday?,
    val cover: FileId?,
    val avatar: FileId?,
)

@S data class UserPreview(
    val id: UserId,
    val firstName: String,
    val lastName: String?,
)
