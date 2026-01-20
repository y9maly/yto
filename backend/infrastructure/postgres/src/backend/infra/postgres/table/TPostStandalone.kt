package backend.infra.postgres.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable


object TPostStandalone : LongIdTable("post__standalone") {
    val text = text("text")
}
