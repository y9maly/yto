package y9to.sdk

import y9to.api.types.File
import y9to.api.types.FileId
import y9to.api.types.FileTypes
import y9to.api.types.UploadFileError
import y9to.libs.stdlib.Union
import y9to.sdk.types.ReadSegmentScope
import y9to.sdk.types.WriteSegmentScope

actual class FileClient internal actual constructor(client: Client) {
    actual suspend fun get(id: FileId): File? {
        TODO("Not yet implemented")
    }

    actual suspend fun download(id: FileId, read: suspend ReadSegmentScope.() -> Unit): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun upload(
        name: String,
        types: FileTypes?,
        expectedSize: Long?,
        write: suspend WriteSegmentScope.() -> Unit
    ): Union<File, UploadFileError> {
        TODO("Not yet implemented")
    }
}
