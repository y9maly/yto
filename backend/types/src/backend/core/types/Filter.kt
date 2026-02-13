package backend.core.types

import backend.core.types.Filter.Blacklist
import backend.core.types.Filter.Whitelist
import kotlinx.serialization.Serializable


@Serializable
sealed interface Filter<out PREDICATE> {
    /**
     * Rejects any except [accept]. Still rejects any [rejectAnyway].
     *
     * I.e. [rejectAnyway] rule has more priority.
     *
     * Pipeline:
     *   > Can reject by [rejectAnyway] rule? If yes - reject.
     *   > Can accept by [accept] rule? If yes - accept.
     *   > Reject.
     */
    @Serializable
    data class Whitelist<out PREDICATE>(
        val accept: Set<PREDICATE> = emptySet(),
        val rejectAnyway: Set<PREDICATE> = emptySet(),
    ) : Filter<PREDICATE>

    /**
     * Accept any except [reject]. Still accepts any [acceptAnyway].
     *
     * I.e. [acceptAnyway] rule has more priority.
     *
     * Pipeline:
     *   > Can accept by [acceptAnyway] rule? If yes - accept.
     *   > Can reject by [reject] rule? If yes - reject.
     *   > Accept.
     */
    @Serializable
    data class Blacklist<out PREDICATE>(
        val reject: Set<PREDICATE> = emptySet(),
        val acceptAnyway: Set<PREDICATE> = emptySet(),
    ) : Filter<PREDICATE>

    companion object {
        internal val AcceptAll = Blacklist<Nothing>()
        internal val RejectAll = Whitelist<Nothing>()
    }
}

val Filter<*>.defaultAccept: Boolean get() = this is Blacklist
val Filter<*>.defaultReject: Boolean get() = this is Whitelist

/**
 * Accepts any
 */
fun acceptAll() = Filter.AcceptAll

/**
 * Rejects any
 */
fun rejectAll() = Filter.RejectAll

/**
 * Reject any except [accept]
 */
fun <PREDICATE> acceptOnly(vararg accept: PREDICATE) = Whitelist(accept = accept.toSet())

/**
 * Accept any except [reject]
 */
fun <PREDICATE> rejectOnly(vararg reject: PREDICATE) = Blacklist(reject = reject.toSet())
