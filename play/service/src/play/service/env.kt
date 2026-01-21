package play.service

import domain.selector.MainSelector
import domain.service.AuthServiceImpl
import domain.service.MainService
import domain.service.PostServiceImpl
import domain.service.UserServiceImpl
import integration.repository.MainRepository
import integration.repository.MainRepositoryPostgres
import kotlinx.coroutines.newSingleThreadContext
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import java.io.File
import kotlin.time.Clock


val database = createDatabase()

val repository = createMainRepository(database)

val selector = MainSelector(repository)

private val clock = Clock.System
val service = MainService(
    AuthServiceImpl(repository, clock),
    UserServiceImpl(repository, selector, clock),
    PostServiceImpl(repository, selector, clock),
)


private fun createDatabase(): R2dbcDatabase {
    return R2dbcDatabase.connect(
        url = "r2dbc:postgresql://postgres:${File("/Users/mali/penis").readText()}@localhost:5050/db",
        driver = "postgresql",
        databaseConfig = R2dbcDatabaseConfig {
            useNestedTransactions = true
            explicitDialect = PostgreSQLDialect()
            sqlLogger = object : SqlLogger {
                override fun log(context: StatementContext, transaction: Transaction) {
//                    println(context.expandArgs(transaction))
                }
            }
        }
    )
}

fun createMainRepository(db: R2dbcDatabase): MainRepository {
    return MainRepositoryPostgres(
        db,
        newSingleThreadContext("Database-Thread"),
//        { event ->
//            println("+EVENT: $event")
//        }
    )
}

