package y9to.libs.stdlib


// todo move outside stdlib


data class Slice<out T>(val list: List<T>, val nextPagingKey: PagingKey?)


sealed interface SpliceKey<out PAGING_OPTIONS> {
    data class Initialize<out PAGING_OPTIONS>(val options: PAGING_OPTIONS) : SpliceKey<PAGING_OPTIONS>
    data class Continue(val pagingKey: PagingKey) : SpliceKey<Nothing>

    fun <R> fold(
        initialize: (PAGING_OPTIONS) -> R,
        next: (pagingKey: PagingKey) -> R,
    ): R = when (this) {
        is Continue -> next(pagingKey)
        is Initialize -> initialize(options)
    }
}


interface PagingKey


fun PagingKey.trySerialize(): SerializedPagingKey? =
    if (this is SerializablePagingKey) serialize()
    else null

fun PagingKey.serializeOrThrow(): SerializedPagingKey =
    trySerialize() ?: error("$this must be serializable, but it doesn't.")

interface SerializablePagingKey : PagingKey {
    fun serialize(): SerializedPagingKey
}

data class SerializedPagingKey(val string: String) : SerializablePagingKey {
    override fun serialize() = this
}






inline fun <T, R> Slice<T>.mapList(transform: (T) -> R): Slice<R> =
    Slice(list.map(transform), nextPagingKey)

inline fun <T> Slice<T>.mapNextPagingKey(transform: (PagingKey) -> PagingKey): Slice<T> =
    Slice(list, nextPagingKey?.let(transform))
