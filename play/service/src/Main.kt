import backend.core.reference.PostReference
import backend.core.reference.UserReference
import backend.core.types.PostId
import backend.core.types.UserId
import kotlinx.coroutines.newSingleThreadContext
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig.Companion.invoke
import java.io.File


suspend fun main() {
    println(service.user.get(UserReference.Random))
    println(service.post.get(PostReference.Random))
    println(service.post.get(PostId(10)))
}
