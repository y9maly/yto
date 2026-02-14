package y9to.api.types

import kotlinx.serialization.Serializable as S
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import y9to.libs.stdlib.optional.present


@S sealed interface Secure<out T> {
    @S data class Available<out T>(val value: T) : Secure<T>

    @S data object Unavailable : Secure<Nothing>

    fun orNull(): T? = (this as? Available)?.value
    fun asOptional(): Optional<T> = when (this) {
        is Available -> present(value)
        Unavailable -> none()
    }
}
