package y9to.api.types

import y9to.common.types.Birthday
import kotlin.jvm.JvmInline
import kotlin.time.Instant


// todo -> UserAccessHash/Ref
@JvmInline
value class UserId(val long: Long) : AuthorizableId

data class User(
    val id: UserId,
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
