package backend.infra.postgres.columnType

import backend.core.types.Revision
import org.jetbrains.exposed.v1.core.Table


fun Table.revision(name: String = "revision") = long(name)
    .transform({ Revision(it) }, { it.long })
