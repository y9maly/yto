package presentation.assembler

import backend.core.types.FileTypes
import presentation.integration.callContext.CallContext
import presentation.mapper.map
import y9to.api.types.FileId
import y9to.api.types.FileType
import y9to.api.types.ImageFile
import y9to.api.types.ImageFileFormat
import backend.core.types.FileId as BackendFileId
import backend.core.types.FileType as BackendFileType
import backend.core.types.ImageFileFormat as BackendImageFileFormat
import backend.core.types.ImageFile as BackendImageFile


class FileAssemblerImpl : FileAssembler {
    context(callContext: CallContext)
    override suspend fun FileId(id: FileId): BackendFileId {
        return id.map()
    }

    context(callContext: CallContext)
    override suspend fun FileType(type: FileType): BackendFileType {
        return type.map()
    }

    context(callContext: CallContext)
    override suspend fun FileTypes(types: y9to.api.types.FileTypes): FileTypes {
        return types.map()
    }
}
