@file:OptIn(ExperimentalContracts::class)

package backend.core.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@S sealed interface Predicate<out C> {
    @SerialName("Criteria")
    @S data class Criteria<out C>(val criteria: C) : Predicate<C>

    @SerialName("True")
    @S data object True : Predicate<Nothing>

    @SerialName("False")
    @S data object False : Predicate<Nothing>

    @SerialName("Not")
    @S data class Not<out C>(val predicate: Predicate<C>) : Predicate<C>

    @SerialName("Or")
    @S data class Or<out C>(val predicates: Set<Predicate<C>>) : Predicate<C>

    @SerialName("And")
    @S data class And<out C>(val predicates: Set<Predicate<C>>) : Predicate<C>

    @SerialName("Xor")
    @S data class Xor<out C>(val firstPredicate: Predicate<C>, val secondPredicate: Predicate<C>) : Predicate<C>
}

fun <C> Predicate(criteria: C): Predicate.Criteria<C> = Predicate.Criteria(criteria)

fun <C, R> Predicate<C>.mapCriteria(transform: (C) -> R): Predicate<R> {
    return MapCriteriaImpl.mapCriteriaInline(this, transform)
}

suspend fun <C, R> Predicate<C>.mapCriteriaSuspend(transform: suspend (C) -> R): Predicate<R> {
    return MapCriteriaImpl.mapCriteriaInline(this) { transform(it) }
}

interface PredicateScope {
    companion object : PredicateScope

    fun <C> not(predicate: Predicate<C>): Predicate.Not<C> {
        return Predicate.Not(predicate)
    }

    infix fun <C> Predicate<C>.or(other: Predicate<C>): Predicate.Or<C> {
        return if (this is Predicate.Or) {
            if (other is Predicate.Or) {
                Predicate.Or(this.predicates + other.predicates)
            } else {
                Predicate.Or(this.predicates + other)
            }
        } else if (other is Predicate.Or) {
            Predicate.Or(other.predicates + this)
        } else {
            Predicate.Or(setOf(this, other))
        }
    }

    infix fun <C> Predicate<C>.and(other: Predicate<C>): Predicate.And<C> {
        return if (this is Predicate.And) {
            if (other is Predicate.And) {
                Predicate.And(this.predicates + other.predicates)
            } else {
                Predicate.And(this.predicates + other)
            }
        } else if (other is Predicate.And) {
            Predicate.And(other.predicates + this)
        } else {
            Predicate.And(setOf(this, other))
        }
    }
}

inline fun <R> PredicateScope(block: PredicateScope.() -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(PredicateScope)
}

val PredicateScope.True get() = Predicate.True
val PredicateScope.False get() = Predicate.False


@PublishedApi
internal object MapCriteriaImpl {
    val deepRecursive = DeepRecursiveFunction<Pair<Predicate<*>, (Any?) -> Any?>, Predicate<*>> { (predicate, transform) ->
        when (predicate) {
            is Predicate.Criteria -> Predicate.Criteria(transform(predicate.criteria))
            Predicate.True, Predicate.False -> predicate
            is Predicate.Not -> Predicate.Not(callRecursive(predicate.predicate to transform))
            is Predicate.Or -> Predicate.And(buildSet {
                predicate.predicates.mapTo(this) { callRecursive(it to transform) }
            })
            is Predicate.And -> Predicate.And(buildSet {
                predicate.predicates.mapTo(this) { callRecursive(it to transform) }
            })
            is Predicate.Xor -> Predicate.Xor(
                firstPredicate = callRecursive(predicate.firstPredicate to transform),
                secondPredicate = callRecursive(predicate.secondPredicate to transform)
            )
        }
    }

    @PublishedApi
    internal enum class Marker { Not, And, Or, Xor }

    inline fun <C, R> mapCriteriaInline(root: Predicate<C>, transform: (C) -> R): Predicate<R> {
        val stack = ArrayDeque<Any>()
        val resultStack = ArrayDeque<Predicate<R>>()

        stack.addLast(root)

        while (stack.isNotEmpty()) {
            when (val current = stack.removeLast()) {
                is Predicate.Criteria<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val node = current as Predicate.Criteria<C>
                    resultStack.addLast(Predicate.Criteria(transform(node.criteria)))
                }
                Predicate.True -> resultStack.addLast(Predicate.True)
                Predicate.False -> resultStack.addLast(Predicate.False)

                is Predicate.Not<*> -> {
                    stack.addLast(Marker.Not)
                    stack.addLast(current.predicate)
                }
                is Predicate.And<*> -> {
                    stack.addLast(current.predicates.size)
                    stack.addLast(Marker.And)
                    for (p in current.predicates) stack.addLast(p)
                }
                is Predicate.Or<*> -> {
                    stack.addLast(current.predicates.size)
                    stack.addLast(Marker.Or)
                    for (p in current.predicates) stack.addLast(p)
                }
                is Predicate.Xor<*> -> {
                    stack.addLast(Marker.Xor)
                    stack.addLast(current.firstPredicate)
                    stack.addLast(current.secondPredicate)
                }

                Marker.Not -> {
                    resultStack.addLast(Predicate.Not(resultStack.removeLast()))
                }
                Marker.And -> {
                    val size = stack.removeLast() as Int
                    val set = LinkedHashSet<Predicate<R>>(size)
                    repeat(size) { set.add(resultStack.removeLast()) }
                    resultStack.addLast(Predicate.And(set))
                }
                Marker.Or -> {
                    val size = stack.removeLast() as Int
                    val set = LinkedHashSet<Predicate<R>>(size)
                    repeat(size) { set.add(resultStack.removeLast()) }
                    resultStack.addLast(Predicate.Or(set))
                }
                Marker.Xor -> {
                    val first = resultStack.removeLast()
                    val second = resultStack.removeLast()
                    resultStack.addLast(Predicate.Xor(first, second))
                }
            }
        }

        return resultStack.removeLast()
    }
}
