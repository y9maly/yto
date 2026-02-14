package y9to.libs.paging

import kotlinx.serialization.Serializable


@Serializable
sealed interface SliceKey<out PAGING_OPTIONS> {
    @Serializable
    data class Initialize<out PAGING_OPTIONS>(val options: PAGING_OPTIONS) : SliceKey<PAGING_OPTIONS>

    @Serializable
    data class Continue(val pagingKey: PagingKey) : SliceKey<Nothing>

    fun <R> fold(
        initialize: (PAGING_OPTIONS) -> R,
        next: (pagingKey: PagingKey) -> R,
    ): R = when (this) {
        is Continue -> next(pagingKey)
        is Initialize -> initialize(options)
    }
}

inline fun <T, R> SliceKey<T>.mapOptions(transform: (T) -> R): SliceKey<R> = when (this) {
    is SliceKey.Continue -> this
    is SliceKey.Initialize -> SliceKey.Initialize(transform(options))
}
