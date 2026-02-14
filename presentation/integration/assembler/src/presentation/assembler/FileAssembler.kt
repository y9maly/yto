@file:Suppress("FunctionName")

package presentation.assembler

import presentation.integration.context.Context
import y9to.api.types.FileId
import y9to.api.types.FileType
import y9to.api.types.FileTypes
import backend.core.types.FileId as BackendFileId
import backend.core.types.FileType as BackendFileType
import backend.core.types.FileTypes as BackendFileTypes


interface FileAssembler {
    context(context: Context)
    suspend fun FileId(id: FileId): BackendFileId

    context(context: Context)
    suspend fun FileType(type: FileType): BackendFileType

    context(context: Context)
    suspend fun FileTypes(types: FileTypes): BackendFileTypes
}

context(_: Context, assembler: FileAssembler)
suspend fun FileId.map(): BackendFileId = assembler.FileId(this)

context(_: Context, assembler: FileAssembler)
suspend fun FileType.map(): BackendFileType = assembler.FileType(this)

context(_: Context, assembler: FileAssembler)
suspend fun FileTypes.map(): BackendFileTypes = assembler.FileTypes(this)
