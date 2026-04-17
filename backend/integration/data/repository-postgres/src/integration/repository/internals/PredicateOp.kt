package integration.repository.internals

import backend.core.types.Predicate
import org.jetbrains.exposed.v1.core.AndOp
import org.jetbrains.exposed.v1.core.NotOp
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.OrOp


internal fun <C> PredicateOp(
    predicate: Predicate<C>,
    criteriaOp: (C) -> Op<Boolean>
): Op<Boolean> {
    return when (predicate) {
        is Predicate.Criteria -> {
            criteriaOp(predicate.criteria)
        }

        Predicate.True -> {
            Op.TRUE
        }

        Predicate.False -> {
            Op.FALSE
        }

        is Predicate.Not -> {
            NotOp(PredicateOp(predicate.predicate, criteriaOp))
        }

        is Predicate.Or -> {
            OrOp(predicate.predicates.map { PredicateOp(it, criteriaOp) })
        }

        is Predicate.And -> {
            AndOp(predicate.predicates.map { PredicateOp(it, criteriaOp) })
        }

        is Predicate.Xor -> {
            val firstOp = PredicateOp(predicate.firstPredicate, criteriaOp)
            val secondOp = PredicateOp(predicate.secondPredicate, criteriaOp)
            OrOp(listOf(
                AndOp(listOf(NotOp(firstOp), secondOp)),
                AndOp(listOf(firstOp, NotOp(secondOp))),
            ))
        }
    }
}
