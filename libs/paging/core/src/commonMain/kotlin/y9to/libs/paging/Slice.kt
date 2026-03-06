package y9to.libs.paging

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(with = Slice.Serializer::class)
interface Slice<out Cursor, out I> {
    val items: List<I>
    val nextCursor: Cursor

    data class Default<out Cursor, out I>(
        override val items: List<I>,
        override val nextCursor: Cursor,
    ) : Slice<Cursor, I> {
        override fun toString() = "Slice(items=$items, nextCursor=$nextCursor)"
    }

    class Serializer<Cursor, I>(
        val cursorSerializer: KSerializer<Cursor>,
        iSerializer: KSerializer<I>,
    ) : KSerializer<Slice<Cursor, I>> {
        private val itemsSerializer = ListSerializer(iSerializer)

        override val descriptor = buildClassSerialDescriptor("Slice") {
            element("items", itemsSerializer.descriptor)
            element("nextCursor", cursorSerializer.descriptor)
        }

        override fun serialize(encoder: Encoder, value: Slice<Cursor, I>) = encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, descriptor.getElementIndex("items"), itemsSerializer, value.items)
            encodeSerializableElement(descriptor, descriptor.getElementIndex("nextCursor"), cursorSerializer, value.nextCursor)
        }

        override fun deserialize(decoder: Decoder): Slice<Cursor, I> = decoder.decodeStructure(descriptor) {
            var items: List<I>? = null
            var nextCursor: Cursor? = null
            var nextCursorField = false

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> {
                        items = decodeSerializableElement(descriptor, 0, itemsSerializer)
                    }

                    1 -> {
                        nextCursor = decodeSerializableElement(descriptor, 1, cursorSerializer)
                        nextCursorField = true
                    }

                    DECODE_DONE -> break

                    else -> throw SerializationException("Unknown index $index")
                }
            }

            @OptIn(ExperimentalSerializationApi::class)
            if (items == null) {
                if (!nextCursorField) {
                    throw MissingFieldException(listOf("items", "nextCursor"), descriptor.serialName)
                } else {
                    throw MissingFieldException(listOf("items"), descriptor.serialName)
                }
            } else if (!nextCursorField) {
                throw MissingFieldException(listOf("nextCursor"), descriptor.serialName)
            }

            @Suppress("UNCHECKED_CAST")
            Slice(items, nextCursor as Cursor)
        }
    }
}

@Serializable(with = Slice2D.Serializer::class)
interface Slice2D<out Cursor, out I> {
    val previousCursor: Cursor
    val items: List<I>
    val nextCursor: Cursor

    data class Default<out Cursor, out I>(
        override val previousCursor: Cursor,
        override val items: List<I>,
        override val nextCursor: Cursor,
    ) : Slice2D<Cursor, I> {
        override fun toString() = "Slice2D(previousCursor=$previousCursor, items=$items, nextCursor=$nextCursor)"
    }

    class Serializer<Cursor, I>(
        val cursorSerializer: KSerializer<Cursor>,
        iSerializer: KSerializer<I>,
    ) : KSerializer<Slice2D<Cursor, I>> {
        private val itemsSerializer = ListSerializer(iSerializer)

        override val descriptor = buildClassSerialDescriptor("Slice2D") {
            element("previousCursor", cursorSerializer.descriptor)
            element("items", itemsSerializer.descriptor)
            element("nextCursor", cursorSerializer.descriptor)
        }

        override fun serialize(encoder: Encoder, value: Slice2D<Cursor, I>) = encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, descriptor.getElementIndex("previousCursor"), cursorSerializer, value.previousCursor)
            encodeSerializableElement(descriptor, descriptor.getElementIndex("items"), itemsSerializer, value.items)
            encodeSerializableElement(descriptor, descriptor.getElementIndex("nextCursor"), cursorSerializer, value.nextCursor)
        }

        override fun deserialize(decoder: Decoder): Slice2D<Cursor, I> = decoder.decodeStructure(descriptor) {
            var previousCursor: Cursor? = null
            var previousCursorField = false
            var items: List<I>? = null
            var nextCursor: Cursor? = null
            var nextCursorField = false

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> {
                        previousCursor = decodeSerializableElement(descriptor, 0, cursorSerializer)
                        previousCursorField = true
                    }

                    1 -> {
                        items = decodeSerializableElement(descriptor, 0, itemsSerializer)
                    }

                    2 -> {
                        nextCursor = decodeSerializableElement(descriptor, 1, cursorSerializer)
                        nextCursorField = true
                    }

                    DECODE_DONE -> break

                    else -> throw SerializationException("Unknown index $index")
                }
            }

            @OptIn(ExperimentalSerializationApi::class)
            if (items == null) {
                if (!nextCursorField) {
                    if (!previousCursorField) {
                        throw MissingFieldException(listOf("previousCursor", "items", "nextCursor"), descriptor.serialName)
                    } else {
                        throw MissingFieldException(listOf("items", "nextCursor"), descriptor.serialName)
                    }
                } else {
                    if (!previousCursorField) {
                        throw MissingFieldException(listOf("previousCursor", "items"), descriptor.serialName)
                    } else {
                        throw MissingFieldException(listOf("items"), descriptor.serialName)
                    }
                }
            } else if (!nextCursorField) {
                if (!previousCursorField) {
                    throw MissingFieldException(listOf("nextCursor"), descriptor.serialName)
                } else {
                    throw MissingFieldException(listOf("previousCursor", "nextCursor"), descriptor.serialName)
                }
            } else if (!previousCursorField) {
                throw MissingFieldException(listOf("previousCursor"), descriptor.serialName)
            }

            @Suppress("UNCHECKED_CAST")
            Slice2D(previousCursor as Cursor, items, nextCursor as Cursor)
        }
    }
}


fun <Cursor, I> Slice(items: List<I>, nextCursor: Cursor): Slice<Cursor, I> =
    Slice.Default(items, nextCursor)

fun <I> Slice(items: List<I>): Slice<Nothing?, I> =
    Slice.Default(items, null)

fun <Cursor, I> Slice2D(previousCursor: Cursor, items: List<I>, nextCursor: Cursor): Slice2D<Cursor, I> =
    Slice2D.Default(previousCursor, items, nextCursor)


inline fun <Cursor, I, R> Slice<Cursor, I>.mapItems(transform: (I) -> R): Slice<Cursor, R> =
    Slice(items.map(transform), nextCursor)

inline fun <Cursor, NewCursor, I> Slice<Cursor, I>.mapNextCursor(transform: (Cursor) -> NewCursor): Slice<NewCursor, I> =
    Slice(items, nextCursor.let(transform))
