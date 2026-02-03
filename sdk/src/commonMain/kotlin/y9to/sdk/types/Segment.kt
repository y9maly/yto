package y9to.sdk.types

import y9to.libs.io.segment.Segment


interface ReadSegmentScope {
    suspend fun read(): Segment?
}

interface WriteSegmentScope {
    suspend fun write(segment: Segment)
}
