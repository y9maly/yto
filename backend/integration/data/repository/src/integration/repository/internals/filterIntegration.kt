package integration.repository.internals

import backend.core.types.Filter
import org.jetbrains.exposed.v1.core.AndOp
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.OrOp
import org.jetbrains.exposed.v1.core.not
import org.jetbrains.exposed.v1.r2dbc.Query
import org.jetbrains.exposed.v1.r2dbc.andWhere


fun <PREDICATE> Query.andFilter(
    filter: Filter<PREDICATE>,
    predicate: (PREDICATE) -> Op<Boolean>
): Query = andWhere { FilterOp(filter, predicate) }


fun <PREDICATE> FilterOp(
    filter: Filter<PREDICATE>,
    predicate: (PREDICATE) -> Op<Boolean>,
): Op<Boolean> {
    return when (filter) {
        is Filter.Whitelist -> FilterOp(filter, predicate)
        is Filter.Blacklist -> FilterOp(filter, predicate)
    }
}

fun <PREDICATE> FilterOp(
    filter: Filter.Whitelist<PREDICATE>,
    predicate: (PREDICATE) -> Op<Boolean>,
): Op<Boolean> {
    val acceptOp = when (filter.accept.size) {
        0 -> return Op.FALSE
        1 -> predicate(filter.accept.single())
        else -> OrOp(filter.accept.map { predicate(it) })
    }

    val rejectAnywayOp = when (filter.rejectAnyway.size) {
        0 -> return acceptOp
        1 -> predicate(filter.rejectAnyway.single())
        else -> OrOp(filter.rejectAnyway.map { predicate(it) })
    }

    return AndOp(listOf(
        acceptOp,
        not(rejectAnywayOp)
    ))
}

fun <PREDICATE> FilterOp(
    filter: Filter.Blacklist<PREDICATE>,
    predicate: (PREDICATE) -> Op<Boolean>,
): Op<Boolean> {
    val rejectOp = when (filter.reject.size) {
        0 -> return Op.TRUE
        1 -> predicate(filter.reject.single())
        else -> OrOp(filter.reject.map { predicate(it) })
    }

    val acceptAnywayOp = when (filter.acceptAnyway.size) {
        0 -> return not(rejectOp)
        1 -> predicate(filter.acceptAnyway.single())
        else -> OrOp(filter.acceptAnyway.map { predicate(it) })
    }

    return OrOp(listOf(
        not(rejectOp),
        acceptAnywayOp
    ))
}
