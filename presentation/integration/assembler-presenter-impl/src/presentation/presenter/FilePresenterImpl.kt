package presentation.presenter

import presentation.integration.callContext.CallContext
import presentation.mapper.map
import y9to.api.types.File
import y9to.api.types.Secure
import backend.core.types.File as BackendFile


class FilePresenterImpl : FilePresenter {
    context(callContext: CallContext)
    override suspend fun File(file: BackendFile): File {
        return File(
            id = file.id.map(),
            name = file.name,
            uploadDate = Secure.Available(file.uploadDate),
            sizeBytes = file.sizeBytes,
        )
    }
}
