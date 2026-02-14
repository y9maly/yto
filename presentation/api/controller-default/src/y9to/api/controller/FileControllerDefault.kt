package y9to.api.controller

import backend.core.types.FileOwner
import domain.service.MainService
import domain.service.result.UploadFilePartsResult
import presentation.assembler.MainAssembler
import presentation.assembler.map
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.presenter.MainPresenter
import presentation.presenter.map
import y9to.api.types.File
import y9to.api.types.FileId
import y9to.api.types.FileSink
import y9to.api.types.FileSource
import y9to.api.types.FileTypes
import y9to.api.types.UploadFileError
import y9to.libs.stdlib.Union
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk


class FileControllerDefault(
    private val service: MainService,
    override val assembler: MainAssembler,
    override val presenter: MainPresenter,
    private val fileSink: (uri: String) -> FileSink,
    private val fileSource: (uri: String) -> FileSource,
) : FileController, ControllerDefault {
    context(_: Context)
    override suspend fun get(id: FileId): File? = context {
        val id = id.map()
        val file = service.file.get(id) ?: return null
        return file.map()
    }

    context(_: Context)
    override suspend fun upload(
        name: String,
        types: FileTypes?,
        expectedSize: Long?
    ): Union<FileSink, UploadFileError> = context {
        val userId = authStateOrPut {
            service.auth.getAuthState(sessionId) ?: error("Unauthenticated")
        }.userIdOrNull() ?: error("Unauthenticated")

        val result = service.file.uploadParts(
            name = name,
            owner = FileOwner.User(userId, sessionId),
            types = types?.map(),
            expectedSize = expectedSize,
        )

        val uri = when (result) {
            is UploadFilePartsResult.Ok ->
                result.uri

            is UploadFilePartsResult.InvalidFileOwner ->
                error("Unauthenticated")

            is UploadFilePartsResult.OwnerStorageQuotaExceeded ->
                return UploadFileError.StorageQuotaExceeded.asError()
        }

        return fileSink(uri).asOk()
    }

    context(_: Context)
    override suspend fun download(id: FileId): FileSource? = context {
        val id = id.map()
        val uri = service.file.get(id)?.uri
            ?: return null
        return fileSource(uri)
    }
}
