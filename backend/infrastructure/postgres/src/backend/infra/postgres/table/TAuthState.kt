package backend.infra.postgres.table

import org.jetbrains.exposed.v1.core.Table


object TAuthState : Table("auth_state") {
    val session = long("session")
    val user = long("user")
}
