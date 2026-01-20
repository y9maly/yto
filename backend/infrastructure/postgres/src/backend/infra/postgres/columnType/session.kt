package backend.infra.postgres.columnType

import backend.core.types.SessionId
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table


fun Table.sessionId(name: String): Column<SessionId> = long(name)
    .transform({ SessionId(it) }, { it.long })
