package y9to.api.types

import kotlinx.serialization.Serializable as S


sealed class ApiException(
    val error: ApiError,
    override val message: String =
        if (error.description != null) "Something went wrong: ${error.description}"
        else "Something went wrong",
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)


@S sealed interface ApiError {
    val description: String? get() = null
}

@S data class InvalidTokenError(val token: Token) : ApiError {
    override val description get() = "Provided token is invalid"
}
