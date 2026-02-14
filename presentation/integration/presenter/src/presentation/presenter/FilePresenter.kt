package presentation.presenter

import presentation.integration.context.Context
import y9to.api.types.File
import y9to.api.types.FileId
import backend.core.types.File as BackendFile
import backend.core.types.FileId as BackendFileId


interface FilePresenter {
    context(context: Context)
    suspend fun FileId(id: BackendFileId): FileId

    context(context: Context)
    suspend fun File(file: BackendFile): File
}


context(_: Context, presenter: FilePresenter)
suspend fun BackendFileId.map(): FileId = presenter.FileId(this)

context(_: Context, presenter: FilePresenter)
suspend fun BackendFile.map(): File = presenter.File(this)

