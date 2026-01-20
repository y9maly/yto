package backend.infra.postgres.columnType

import backend.core.types.PostContentType
import backend.infra.postgres.backendType.dbName
import backend.infra.postgres.util.enumerationBy
import org.jetbrains.exposed.v1.core.Table


fun Table.postContentType(name: String) =
    enumerationBy<PostContentType>(name, "post_content_type") { it.dbName }
