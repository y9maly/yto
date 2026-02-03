@file:Suppress("FunctionName")

package presentation.assembler

import presentation.integration.callContext.CallContext
import y9to.api.types.FileId
import y9to.api.types.FileType
import y9to.api.types.FileTypes
import y9to.api.types.ImageFile
import y9to.api.types.ImageFileFormat
import backend.core.types.FileId as BackendFileId
import backend.core.types.FileType as BackendFileType
import backend.core.types.FileTypes as BackendFileTypes
import backend.core.types.ImageFileFormat as BackendImageFileFormat
import backend.core.types.ImageFile as BackendImageFile


interface FileAssembler {
    context(callContext: CallContext)
    suspend fun FileId(id: FileId): BackendFileId

    context(callContext: CallContext)
    suspend fun FileType(type: FileType): BackendFileType

    context(callContext: CallContext)
    suspend fun FileTypes(types: FileTypes): BackendFileTypes
}
