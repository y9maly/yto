package backend.infra.postgres.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp


object TFile : LongIdTable("file") {
    val uri = varchar("uri", 512)
    val upload_date = timestamp("upload_date")
    val expires_at = timestamp("expires_at").nullable()
    val name = varchar("name", 128)
        .nullTransform({ it.takeIf { it.isNotEmpty() } }, { it ?: "" })
    val size_bytes = long("size_bytes")
    val owner_session = long("owner_session").nullable()
    val owner_user = long("owner_user").nullable()
    val owner_user_grave = long("owner_user_grave").nullable()
}
