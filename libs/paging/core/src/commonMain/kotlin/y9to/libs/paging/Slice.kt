package y9to.libs.paging

import kotlinx.serialization.Serializable


@Serializable
data class Slice<out T>(val list: List<T>, val nextPagingKey: PagingKey?)

@Serializable
data class Slice2D<out T>(val previousPagingKey: PagingKey?, val list: List<T>, val nextPagingKey: PagingKey?)


inline fun <T, R> Slice<T>.mapList(transform: (T) -> R): Slice<R> =
    Slice(list.map(transform), nextPagingKey)

inline fun <T> Slice<T>.mapNextPagingKey(transform: (PagingKey) -> PagingKey): Slice<T> =
    Slice(list, nextPagingKey?.let(transform))
