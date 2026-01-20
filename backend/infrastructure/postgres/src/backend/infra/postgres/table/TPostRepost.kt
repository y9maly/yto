package backend.infra.postgres.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable


object TPostRepost : LongIdTable("post__repost") {
    val comment = text("comment").nullable()
    val original = long("original").nullable()
    val original_grave = long("original_grave").nullable()
}
