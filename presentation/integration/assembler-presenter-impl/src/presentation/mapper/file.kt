package presentation.mapper

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


internal fun FileId.map() = BackendFileId(long)
internal fun BackendFileId.map() = FileId(long)
internal fun ImageFileFormat.map() = BackendImageFileFormat(name)
internal fun BackendImageFileFormat.map() = ImageFileFormat(name)

internal fun FileType.map(): BackendFileType = when (this) {
    is ImageFile -> map()
}

internal fun ImageFile.map() = BackendImageFile(
    file = file.map(),
    format = format.map(),
    width = width,
    height = height,
)

internal fun FileTypes.map() = BackendFileTypes(
    image = image?.map(),
)
