package y9to.libs.stdlib


inline fun <T, R> T.letCatching(block: (T) -> R): Result<R> {
    return runCatching { block(this) }
}

inline fun <T> T.alsoCatching(block: (T) -> Unit): Result<T> {
    val result = runCatching { block(this) }
    result.onFailure {
        return Result.failure(it)
    }
    return Result.success(this)
}

inline fun <T> T.applyCatching(block: T.() -> Unit): Result<T> {
    val result = runCatching { block(this) }
    result.onFailure {
        return Result.failure(it)
    }
    return Result.success(this)
}
