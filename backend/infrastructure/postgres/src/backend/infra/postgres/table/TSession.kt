package backend.infra.postgres.table

import backend.infra.postgres.columnType.revision
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp


object TSession : LongIdTable("session") {
    val revision = revision()
    val creation_date = timestamp("creation_date")
}
