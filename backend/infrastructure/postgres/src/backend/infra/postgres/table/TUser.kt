package backend.infra.postgres.table

import backend.infra.postgres.columnType.birthday
import backend.infra.postgres.columnType.revision
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp


object TUser : LongIdTable("users") {
    val revision = revision()
    val registration_date = timestamp("registration_date")
    val telegram_auth_id = varchar("telegram_auth_id", 255).nullable()
    val telegram_user_id = long("telegram_user_id").nullable()
    val phone_number = varchar("phone_number", 32).nullable()
    val email = varchar("email", 128).nullable()
    val password_plaintext = varchar("password_plaintext", 256).nullable()
    val first_name = varchar("first_name", 64)
    val last_name = varchar("last_name", 64).nullable()
    val bio = varchar("bio", 1024).nullable()
    val birthday = birthday("birthday").nullable()
    val cover = long("cover").nullable()
    val avatar = long("avatar").nullable()
}
