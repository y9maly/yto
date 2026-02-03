@file:OptIn(DelicateIoApi::class)

package y9to.libs.io.segment

import y9to.libs.io.internals.DelicateIoApi


fun MutableSegment(
    byteArray: ByteArray,
    start: Int = 0,
    size: Int = byteArray.size - start,
) = Segment(byteArray, false, start, size)

fun ImmutableSegment(
    byteArray: ByteArray,
    start: Int = 0,
    size: Int = byteArray.size - start,
) = Segment(byteArray, true, start, size)

/**
 * Copies entire RAM area and creates a new mutable Segment
 */
fun Segment.cloneAsMutable() = MutableSegment(byteArray.copyOfRange(start, end))

/**
 * Copies entire RAM area and creates a new immutable Segment
 */
fun Segment.cloneAsImmutable() = ImmutableSegment(byteArray.copyOfRange(start, end))

/**
 * Copies entire RAM area if necessary.
 * - If immutable: returns the same segment
 * - If mutable: Copies entire RAM area and creates a new immutable Segment
 * @return Immutable segment
 */
fun Segment.snapshot() =
    if (immutable) this
    else cloneAsImmutable()

/**
 * Creates the same segment that refers to the same RAM area but marked as immutable.
 * Return the same segment if it is already immutable.
 */
@DelicateIoApi
fun Segment.unsafeAsImmutable() =
    if (immutable) this
    else ImmutableSegment(byteArray, start, size)
