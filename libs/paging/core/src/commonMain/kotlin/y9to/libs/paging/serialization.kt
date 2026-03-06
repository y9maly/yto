package y9to.libs.paging

import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString


inline fun <reified T : Any> Cursor.Companion.encodePayloadIfNotNull(format: StringFormat, payload: T?): Cursor? {
    if (payload == null) return null
    return encodePayload(format, payload)
}

inline fun <reified T> Cursor.Companion.encodePayload(format: StringFormat, payload: T): Cursor {
    return Cursor(format.encodeToString(payload))
}

inline fun <reified NewCursor, Options> SliceKey<Options, Cursor>.decodePayload(
    format: StringFormat
): SliceKey<Options, NewCursor> {
    return mapCursor { cursor ->
        format.decodeFromString<NewCursor>(cursor.payload)
    }
}
