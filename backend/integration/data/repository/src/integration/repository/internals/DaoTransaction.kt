package integration.repository.internals

import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.v1.core.Transaction


internal class DaoTransaction(
    scope: CoroutineScope,
    val exposedTransaction: Transaction,
) : CoroutineScope by scope
