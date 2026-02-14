package presentation.presenter

import presentation.integration.context.Context
import presentation.mapper.map
import y9to.api.types.File
import y9to.api.types.FileId
import y9to.api.types.Secure
import backend.core.types.FileId as BackendFileId
import backend.core.types.File as BackendFile


class FilePresenterImpl : FilePresenter {
    context(context: Context)
    override suspend fun FileId(id: BackendFileId): FileId {
        return id.map()
    }

    context(context: Context)
    override suspend fun File(file: BackendFile): File {
        return File(
            id = file.id.map(),
            name = file.name,
            uploadDate = Secure.Available(file.uploadDate),
            sizeBytes = file.sizeBytes,
        )
    }
}
