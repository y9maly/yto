package integration.repository.internals

import org.jetbrains.exposed.v1.core.Transaction


internal data class DaoTransaction(
    val exposedTransaction: Transaction,
)
