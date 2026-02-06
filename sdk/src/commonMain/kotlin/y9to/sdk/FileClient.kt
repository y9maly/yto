package y9to.sdk

import kotlinx.io.Source
import y9to.api.types.File
import y9to.api.types.FileId
import y9to.api.types.FileTypes
import y9to.api.types.UploadFileError
import y9to.libs.io.segment.MutableSegment
import y9to.libs.stdlib.Union
import y9to.sdk.types.ReadSegmentScope
import y9to.sdk.types.WriteSegmentScope


expect class FileClient internal constructor(
    client: Client,
) {
    suspend fun get(id: FileId): File?

    /**
     * @return false if [sink] was not called and file id is invalid. true is [sink] was called.
     */
    suspend fun download(id: FileId, read: suspend ReadSegmentScope.() -> Unit): Boolean

    suspend fun upload(
        name: String,
        types: FileTypes? = null,
        expectedSize: Long? = null,
        write: suspend WriteSegmentScope.() -> Unit,
    ): Union<File, UploadFileError>
}

private const val BUFFER_SIZE = 1024 * 1024 // 1mb

suspend fun FileClient.upload(
    name: String,
    types: FileTypes? = null,
    expectedSize: Long? = null,
    source: Source,
): Union<File, UploadFileError> {
    return upload(
        name = name,
        types = types,
        expectedSize = expectedSize,
    ) {
        val buffer = ByteArray(BUFFER_SIZE)

        while (!source.exhausted()) {
            val readedBytes = source.readAtMostTo(buffer)
            if (readedBytes == -1) break
            write(MutableSegment(buffer, start = 0, size = readedBytes))
        }
    }
}
