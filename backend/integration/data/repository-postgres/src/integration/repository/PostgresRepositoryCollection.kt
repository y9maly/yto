package integration.repository

import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import kotlin.coroutines.CoroutineContext


fun PostgresRepositoryCollection(
    database: R2dbcDatabase,
    transactionCoroutineContext: CoroutineContext,
): RepositoryCollection = with(
    MainRepository(database, transactionCoroutineContext)
) {
    return RepositoryCollection(
        auth = auth,
        user = user,
        post = post,
        file = file,
    )
}
