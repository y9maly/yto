package backend.infra.postgres.columnType

import backend.core.types.UserId
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table


fun Table.userId(name: String): Column<UserId> = long(name)
    .transform({ UserId(it) }, { it.long })
