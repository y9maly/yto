package backend.infra.postgres.view

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp


object VGravePost : LongIdTable("view_grave_post") {
    val created_at = timestamp("created_at")
    val deleted_at = timestamp("deleted_at")
    val last_edit_date = timestamp("last_edit_date")

    // author
    val author_id = long("author_id")
    val author_first_name = text("author_first_name")
    val author_last_name = text("author_last_name").nullable()
}
