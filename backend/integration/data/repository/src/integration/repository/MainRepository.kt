package integration.repository

import integration.repository.internals.DaoTransaction
import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.transactions.transactionManager
import y9to.libs.stdlib.InterfaceClass
import kotlin.coroutines.CoroutineContext


internal val ReadOnly = MainRepository.ReadOnly

class MainRepository @InterfaceClass constructor(
    private val database: R2dbcDatabase,
    private val transactionCoroutineContext: CoroutineContext,
//    internal val eventsCollector: EventsCollector<Any>,
) {
    val auth = AuthRepository(this)
    val user = UserRepository(this)
    val post = PostRepository(this)

    internal object ReadOnly

    internal suspend fun <T> transaction(
        transactionIsolation: IsolationLevel? = database.transactionManager.defaultIsolationLevel,
        block: suspend DaoTransaction.() -> T
    ): T = withContext(transactionCoroutineContext) {
        suspendTransaction(transactionIsolation = transactionIsolation) {
            block(DaoTransaction(TransactionManager.current()))
        }
    }

    internal suspend fun <T> transaction(
        readOnly: ReadOnly,
        transactionIsolation: IsolationLevel? = database.transactionManager.defaultIsolationLevel,
        block: suspend DaoTransaction.() -> T
    ): T = withContext(transactionCoroutineContext) {
        suspendTransaction(transactionIsolation = transactionIsolation, readOnly = true) {
            block(DaoTransaction(TransactionManager.current()))
        }
    }
}
