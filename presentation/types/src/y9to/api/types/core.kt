package y9to.api.types

import kotlinx.serialization.Serializable
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import y9to.libs.stdlib.optional.present


@Serializable
sealed interface Secure<out T> {
    @Serializable
    data class Available<out T>(val value: T) : Secure<T>

    @Serializable
    data object Unavailable : Secure<Nothing>

    fun orNull(): T? = (this as? Available)?.value
    fun asOptional(): Optional<T> = when (this) {
        is Available -> present(value)
        Unavailable -> none()
    }
}
