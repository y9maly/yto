package backend.infra.postgres.columnType

import backend.core.types.Revision
import backend.core.types.TemporaryRevisionApi
import org.jetbrains.exposed.v1.core.Table


@OptIn(TemporaryRevisionApi::class)
fun Table.revision(name: String = "revision") = long(name)
    .transform({ Revision(it) }, { it.long })
