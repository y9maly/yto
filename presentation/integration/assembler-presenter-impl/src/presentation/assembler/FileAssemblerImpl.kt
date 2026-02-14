package presentation.assembler

import backend.core.types.FileTypes
import presentation.integration.context.Context
import presentation.mapper.map
import y9to.api.types.FileId
import y9to.api.types.FileType
import backend.core.types.FileId as BackendFileId
import backend.core.types.FileType as BackendFileType


class FileAssemblerImpl : FileAssembler {
    context(context: Context)
    override suspend fun FileId(id: FileId): BackendFileId {
        return id.map()
    }

    context(context: Context)
    override suspend fun FileType(type: FileType): BackendFileType {
        return type.map()
    }

    context(context: Context)
    override suspend fun FileTypes(types: y9to.api.types.FileTypes): FileTypes {
        return types.map()
    }
}
