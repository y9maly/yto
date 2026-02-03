package presentation.presenter

import presentation.integration.callContext.CallContext
import y9to.api.types.File
import backend.core.types.File as BackendFile


interface FilePresenter {
    context(callContext: CallContext)
    suspend fun File(file: BackendFile): File
}
