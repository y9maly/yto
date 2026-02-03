package presentation.api.krpc

import backend.core.types.FileOwner
import domain.service.FileService
import domain.service.MainService
import domain.service.result.UploadFilePartsResult
import presentation.api.krpc.internals.authenticate
import presentation.assembler.FileAssembler
import presentation.assembler.MainAssembler
import presentation.authenticator.Authenticator
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.authStateOrPut
import presentation.integration.callContext.elements.sessionId
import presentation.presenter.MainPresenter
import y9to.api.krpc.FileRpc
import y9to.api.krpc.types.FileSink
import y9to.api.krpc.types.FileSource
import y9to.api.types.File
import y9to.api.types.FileId
import y9to.api.types.FileTypes
import y9to.api.types.Token
import y9to.api.types.UploadFileError
import y9to.libs.stdlib.Union
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk


class FileRpcImpl(
    private val authenticator: Authenticator,
    private val service: MainService,
    private val assembler: MainAssembler,
    private val presenter: MainPresenter,
    private val fileSink: (uri: String) -> FileSink,
    private val fileSource: (uri: String) -> FileSource,
) : FileRpc {
    override suspend fun get(token: Token, id: FileId): File? = authenticate(token) {
        val id = assembler.file.FileId(id)
        val file = service.file.get(id) ?: return null
        return presenter.file.File(file)
    }

    override suspend fun upload(
        token: Token,
        name: String,
        types: FileTypes?,
        expectedSize: Long?,
    ): Union<FileSink, UploadFileError> = authenticate(token) {
        val userId = authStateOrPut {
            service.auth.getAuthState(sessionId) ?: error("Unauthenticated")
        }.userIdOrNull() ?: error("Unauthenticated")

        val result = service.file.uploadParts(
            name = name,
            owner = FileOwner.User(userId, sessionId),
            types = types?.run {
                assembler.file.FileTypes(types)
            },
            expectedSize = expectedSize,
        )

        val uri = when (result) {
            is UploadFilePartsResult.Ok ->
                result.uri

            is UploadFilePartsResult.InvalidFileOwner ->
                error("Unauthenticated")

            is UploadFilePartsResult.OwnerStorageQuotaExceeded ->
                return@authenticate UploadFileError.StorageQuotaExceeded.asError()
        }

        fileSink(uri).asOk()
    }

    override suspend fun download(token: Token, id: FileId): FileSource? = authenticate(token) {
        val id = assembler.file.FileId(id)
        val uri = service.file.get(id)?.uri
            ?: return@authenticate null
        fileSource(uri)
    }

    private suspend inline fun <R> authenticate(token: Token, block: CallContext.() -> R) =
        authenticate(authenticator, token, block)
}
