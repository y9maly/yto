package integration.repository

import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import y9to.libs.stdlib.InterfaceClass
import kotlin.coroutines.CoroutineContext


@OptIn(InterfaceClass::class)
fun MainRepositoryPostgres(
    database: R2dbcDatabase,
    transactionCoroutineContext: CoroutineContext,
//    eventsCollector: EventsCollector<Any>,
): MainRepository {
    return MainRepository(database, transactionCoroutineContext)
}
