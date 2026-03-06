package y9to.api.y9to.api.endpoint

import y9to.api.types.*
import y9to.libs.stdlib.Union


interface FileEndpoint {
    fun get(id: FileId): File?

    fun upload(
        name: String,
        types: FileTypes?,
        expectedSize: Long?,
    ): Union<FileSink, UploadFileError>

    fun download(id: FileId): FileSource?
}
