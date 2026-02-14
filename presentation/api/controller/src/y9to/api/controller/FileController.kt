package y9to.api.controller

import presentation.integration.context.Context
import y9to.api.types.File
import y9to.api.types.FileId
import y9to.api.types.FileSink
import y9to.api.types.FileSource
import y9to.api.types.FileTypes
import y9to.api.types.Token
import y9to.api.types.UploadFileError
import y9to.libs.stdlib.Union


interface FileController {
    context(_: Context) suspend fun get(id: FileId): File?

    context(_: Context) suspend fun upload(
        name: String,
        types: FileTypes?,
        expectedSize: Long?,
    ): Union<FileSink, UploadFileError>

    context(_: Context) suspend fun download(id: FileId): FileSource?
}
