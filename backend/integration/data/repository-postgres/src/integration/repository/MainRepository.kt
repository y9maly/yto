package integration.repository

import integration.repository.internals.DaoTransaction
import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.transactions.transactionManager
import kotlin.coroutines.CoroutineContext


internal val ReadOnly = MainRepository.ReadOnlyTransactionMarker

internal class MainRepository(
    private val database: R2dbcDatabase,
    private val transactionCoroutineContext: CoroutineContext,
) {
    val auth = PostgresAuthRepository(this)
    val user = PostgresUserRepository(this)
    val post = PostgresPostRepository(this)
    val file = PostgresFileRepository(this)

    internal object ReadOnlyTransactionMarker

    internal suspend fun <T> transaction(
        transactionIsolation: IsolationLevel? = database.transactionManager.defaultIsolationLevel,
        block: suspend DaoTransaction.() -> T
    ): T = withContext(transactionCoroutineContext) {
        suspendTransaction(transactionIsolation = transactionIsolation) {
            coroutineScope {
                block(DaoTransaction(this, TransactionManager.current()))
            }
        }
    }

    internal suspend fun <T> transaction(
        readOnly: ReadOnlyTransactionMarker,
        transactionIsolation: IsolationLevel? = database.transactionManager.defaultIsolationLevel,
        block: suspend DaoTransaction.() -> T
    ): T = withContext(transactionCoroutineContext) {
        suspendTransaction(transactionIsolation = transactionIsolation, readOnly = true) {
            coroutineScope {
                block(DaoTransaction(this, TransactionManager.current()))
            }
        }
    }
}
