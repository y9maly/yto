package backend.infra.postgres.table

import backend.infra.postgres.columnType.postContentType
import backend.infra.postgres.columnType.revision
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp


object TPost : LongIdTable("post") {
    val revision = revision()
    val location_global = bool("location_global")
    val location_profile = long("location_profile").nullable()
    val created_at = timestamp("created_at")
    val author = long("author")
    val reply_to = long("reply_to").nullable()
    val reply_to_grave = long("reply_to_grave").nullable()
    val last_edit_date = timestamp("last_edit_date").nullable()
    val content_type = postContentType("content_type")
}
