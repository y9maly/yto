package y9to.api.krpc

import kotlinx.rpc.annotations.Rpc
import y9to.api.types.*
import y9to.libs.stdlib.Union


@Rpc
interface FileRpc {
    suspend fun get(token: Token, id: FileId): File?

    suspend fun upload(
        token: Token,
        name: String,
        types: FileTypes?,
        expectedSize: Long?,
    ): Union<FileSink, UploadFileError>

    suspend fun download(token: Token, id: FileId): FileSource?
}
