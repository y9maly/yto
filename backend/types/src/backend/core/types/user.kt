package backend.core.types

import y9to.common.types.Birthday
import kotlin.time.Instant


@JvmInline
value class UserId(val long: Long) : ClientId

data class User(
    val id: UserId,
    val revision: Revision,
    val registrationDate: Instant,
    val firstName: String,
//    val avatar: AnyContentId?,
//    val header: AnyContentId?,
    val lastName: String?,
    val phoneNumber: String?,
    val email: String?,
    val bio: String?,
    val birthday: Birthday?,
)

data class UserPreview(
    val id: UserId,
    val firstName: String,
    val lastName: String?,
)
