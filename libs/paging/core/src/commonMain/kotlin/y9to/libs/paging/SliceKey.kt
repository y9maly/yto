package y9to.libs.paging

import kotlinx.serialization.Serializable


@Serializable
sealed interface SliceKey<out Options, out Cursor> {
    companion object {
        fun Initialize() = Initialize(Unit)
    }

    @Serializable
    data class Initialize<out Options>(val options: Options) : SliceKey<Options, Nothing>

    @Serializable
    data class Next<out Cursor>(val cursor: Cursor) : SliceKey<Nothing, Cursor>
}

fun <Options> SliceKey<Options, *>.optionsOrNull(): Options? = (this as? SliceKey.Initialize)?.options
fun <Cursor> SliceKey<*, Cursor>.cursorOrNull(): Cursor? = (this as? SliceKey.Next)?.cursor

fun <Options> SliceKey<Options, *>.options(): Options = (this as SliceKey.Initialize).options
fun <Cursor> SliceKey<*, Cursor>.cursor(): Cursor = (this as SliceKey.Next).cursor

inline fun <Options, Cursor, R> SliceKey<Options, Cursor>.fold(
    initialize: (Options) -> R,
    next: (cursor: Cursor) -> R,
): R = when (this) {
    is SliceKey.Next -> next(cursor)
    is SliceKey.Initialize -> initialize(options)
}

@Suppress("UNCHECKED_CAST")
inline fun <Key : SliceKey<Options, *>, Options> Key.onInitialize(
    block: (Options) -> Unit,
): Key = when (this) {
    is SliceKey.Next<*> -> this
    is SliceKey.Initialize<*> -> apply { block(options as Options) }
}

@Suppress("UNCHECKED_CAST")
inline fun <Key : SliceKey<*, Cursor>, Cursor> Key.onNext(
    block: (Cursor) -> Unit,
): Key = when (this) {
    is SliceKey.Next<*> -> apply { block(cursor as Cursor) }
    is SliceKey.Initialize<*> -> this
}

inline fun <Options, NewOptions, Cursor> SliceKey<Options, Cursor>.mapOptions(
    transform: (Options) -> NewOptions,
): SliceKey<NewOptions, Cursor> = when (this) {
    is SliceKey.Next -> this
    is SliceKey.Initialize -> SliceKey.Initialize(transform(options))
}

inline fun <Options, Cursor, NewCursor> SliceKey<Options, Cursor>.mapCursor(
    transform: (Cursor) -> NewCursor,
): SliceKey<Options, NewCursor> = when (this) {
    is SliceKey.Next -> SliceKey.Next(transform(cursor))
    is SliceKey.Initialize -> this
}
