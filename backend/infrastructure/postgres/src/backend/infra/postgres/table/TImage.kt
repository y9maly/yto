package backend.infra.postgres.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable


object TImage : LongIdTable("image", columnName = "file") {
    val format = varchar("format", 8)
    val width = integer("width").nullable()
    val height = integer("height").nullable()
}
