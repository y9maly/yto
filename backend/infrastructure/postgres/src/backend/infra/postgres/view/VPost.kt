package backend.infra.postgres.view

import backend.infra.postgres.columnType.postContentType
import backend.infra.postgres.columnType.revision
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp


object VPost : LongIdTable("view_post") {
    val revision = revision()
    val created_at = timestamp("created_at")
    val last_edit_date = timestamp("last_edit_date").nullable()
    val content_type = postContentType("content_type")

    // author
    val author_id = long("author_id")
    val author_first_name = text("author_first_name")
    val author_last_name = text("author_last_name").nullable()

    // reply header
    val reply_id = long("reply_id").nullable()
    val reply_created_at = timestamp("reply_created_at").nullable()
    val reply_deleted_at = timestamp("reply_deleted_at").nullable()
    val reply_author_id = long("reply_author_id").nullable()
    val reply_author_first_name = text("reply_author_first_name").nullable()
    val reply_author_last_name = text("reply_author_last_name").nullable()

    // standalone
    val standalone_text = text("standalone_text").nullable()

    // repost
    val repost_comment = text("repost_comment").nullable()
    val repost_original_id = long("repost_original_id").nullable()
    val repost_original_created_at = timestamp("repost_original_created_at").nullable()
    val repost_original_deleted_at = timestamp("repost_original_deleted_at").nullable()
    val repost_original_last_edit_date = timestamp("repost_original_last_edit_date").nullable()
    val repost_original_author_id = long("repost_original_author_id").nullable()
    val repost_original_author_first_name = text("repost_original_author_first_name").nullable()
    val repost_original_author_last_name = text("repost_original_author_last_name").nullable()
}
