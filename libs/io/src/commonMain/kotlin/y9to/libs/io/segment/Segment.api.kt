package y9to.libs.io.segment


inline val Segment.mutable get() = !immutable
inline val Segment.end get() = start + size

