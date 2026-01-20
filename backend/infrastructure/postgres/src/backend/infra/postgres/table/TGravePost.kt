package backend.infra.postgres.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp


object TGravePost : LongIdTable("grave_post") {
    val created_at = timestamp("created_at")
    val deleted_at = timestamp("deleted_at")
    val author = long("author").nullable()
    val author_grave = long("author_grave").nullable()
}
