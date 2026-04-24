@file:JvmName("TypeUserKt")

package y9to.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import y9to.common.types.Birthday
import kotlin.jvm.JvmName
import kotlin.time.Instant


@SerialName("UserId")
@S data class UserId(val long: Long) : InputUser, ClientId

@S data class User(
    val id: UserId,
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

@S data class MyProfile(
    val id: UserId,
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
