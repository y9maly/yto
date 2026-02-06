package presentation.presenter

import presentation.integration.callContext.CallContext
import y9to.api.types.File
import y9to.api.types.FileId
import backend.core.types.FileId as BackendFileId
import backend.core.types.File as BackendFile


interface FilePresenter {
    context(callContext: CallContext)
    suspend fun FileId(id: BackendFileId): FileId

    context(callContext: CallContext)
    suspend fun File(file: BackendFile): File
}
