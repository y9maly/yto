@file:JvmName("TypeUserKt")

package y9to.api.types

import kotlinx.serialization.Serializable
import y9to.common.types.Birthday
import kotlin.jvm.JvmName
import kotlin.time.Instant


// todo -> UserAccessHash/Ref
/**
 * Почему это data class?
 *
 * ```Kotlin
 * @Serializable
 * sealed interface A
 *
 * @Serializable
 * @JvmInline
 * value class B(val int: Int) : A
 *
 * Json.encodeToString(serializer<A>(), B(123))
 * ```
 *
 * Котлин дебил и сериализует это как "123", и очевидно падает при десериализации этого.
 */
@Serializable
data class UserId(val long: Long) : ClientId

@Serializable
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

@Serializable
data class MyProfile(
    val id: UserId,
    val registrationDate: Instant,
    val firstName: String,
    val lastName: String?,
    val phoneNumber: String?,
    val email: String?,
    val bio: String?,
    val birthday: Birthday?,
)

@Serializable
data class UserPreview(
    val id: UserId,
    val firstName: String,
    val lastName: String?,
)
