package backend.infra.postgres.table

import backend.infra.postgres.columnType.birthday
import backend.infra.postgres.columnType.revision
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp


object TUser : LongIdTable("users") {
    val revision = revision()
    val registration_date = timestamp("registration_date")
    val phone_number = varchar("phone_number", 32).nullable()
    val email = varchar("email", 128).nullable()
    val password_plaintext = varchar("password_plaintext", 256).nullable()
//    val header = anyContentId("header").nullable()
//    val avatar = anyContentId("avatar").nullable()
    val first_name = varchar("first_name", 64)
    val last_name = varchar("last_name", 64).nullable()
    val bio = varchar("bio", 1024).nullable()
    val birthday = birthday("birthday").nullable()
}
