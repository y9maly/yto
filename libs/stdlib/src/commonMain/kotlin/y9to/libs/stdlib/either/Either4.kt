// USELESS_CAST нужен из-за иногда возникающих ошибок (фронт компилятора?):
// actual type is T, but T was expected
// которые фиксятся при ctrl + a; ctrl + x; ctrl + v;
@file:Suppress("NOTHING_TO_INLINE", "unused", "UNCHECKED_CAST", "USELESS_CAST")

package y9to.libs.stdlib.either

import kotlin.jvm.JvmInline


@PublishedApi internal const val Either4_VARIANT_SECOND = 0
@PublishedApi internal const val Either4_VARIANT_THIRD = 1
@PublishedApi internal const val Either4_VARIANT_FOURTH = 2

@JvmInline
value class Either4<out FIRST, out SECOND, out THIRD, out FOURTH>(@PublishedApi internal val value: Any?) {
    companion object {
        inline fun <VALUE> first(value: VALUE) = Either4<VALUE, Nothing, Nothing, Nothing>(value)
        inline fun <VALUE> second(value: VALUE) = Either4<Nothing, VALUE, Nothing, Nothing>(Other(Either4_VARIANT_SECOND, value))
        inline fun <VALUE> third(value: VALUE) = Either4<Nothing, Nothing, VALUE, Nothing>(Other(Either4_VARIANT_THIRD, value))
        inline fun <VALUE> fourth(value: VALUE) = Either4<Nothing, Nothing, Nothing, VALUE>(Other(Either4_VARIANT_FOURTH, value))
    }

    /**
     * [variant] будет number в JS.
     * Хоть он и занимает больше байтиков, чем, например, true false null, это предпочтительнее для JIT компиляций
     */
    @PublishedApi
    internal class Other(val variant: Int, val value: Any?)

    inline val isFirst: Boolean get() = value !is Other
    inline val isSecond: Boolean get() = value is Other && value.variant == Either4_VARIANT_SECOND
    inline val isThird: Boolean get() = value is Other && value.variant == Either4_VARIANT_THIRD
    inline val isFourth: Boolean get() = value is Other && value.variant == Either4_VARIANT_FOURTH

    override fun toString() =
        if (isFirst) "Either4.First($value)"
        else if (isSecond) "Either4.Second($value)"
        else if (isThird) "Either4.Third($value)"
        else "Either4.Fourth($value)"
}

inline fun <FIRST> Either4<FIRST, *, *, *>.asFirst(): FIRST =
    if (isFirst) value as FIRST
    else throw EitherCastException()

inline fun <SECOND> Either4<*, SECOND, *, *>.asSecond(): SECOND =
    if (isSecond) value as SECOND
    else throw EitherCastException()

inline fun <THIRD> Either4<*, *, THIRD, *>.asThird(): THIRD =
    if (isThird) value as THIRD
    else throw EitherCastException()

inline fun <FOURTH> Either4<*, *, *, FOURTH>.asFourth(): FOURTH =
    if (isFourth) value as FOURTH
    else throw EitherCastException()


inline fun <FIRST> Either4<FIRST, *, *, *>.firstOrNull(): FIRST? =
    if (isFirst) value as FIRST
    else null

inline fun <SECOND> Either4<*, SECOND, *, *>.secondOrNull(): SECOND? =
    if (isSecond) value as SECOND
    else null

inline fun <THIRD> Either4<*, *, THIRD, *>.thirdOrNull(): THIRD? =
    if (isThird) value as THIRD
    else null

inline fun <FOURTH> Either4<*, *, *, FOURTH>.fourthOrNull(): FOURTH? =
    if (isFourth) value as FOURTH
    else null


inline fun <FIRST> Either4<FIRST, *, *, *>.firstOrDefault(get: () -> FIRST): FIRST =
    if (isFirst) value as FIRST
    else get()

inline fun <SECOND> Either4<*, SECOND, *, *>.secondOrDefault(get: () -> SECOND): SECOND =
    if (isSecond) value as SECOND
    else get()

inline fun <THIRD> Either4<*, *, THIRD, *>.thirdOrDefault(get: () -> THIRD): THIRD =
    if (isThird) value as THIRD
    else get()

inline fun <FOURTH> Either4<*, *, *, FOURTH>.fourthOrDefault(get: () -> FOURTH): FOURTH =
    if (isFourth) value as FOURTH
    else get()


inline fun <FIRST : R, ELSE : R, R> Either4<FIRST, *, *, *>.firstOrElse(get: () -> ELSE): R =
    if (isFirst) value as FIRST as R
    else get() as R

inline fun <SECOND : R, ELSE : R, R> Either4<*, SECOND, *, *>.secondOrElse(get: () -> ELSE): R =
    if (isSecond) value as SECOND as R
    else get() as R

inline fun <THIRD : R, ELSE : R, R> Either4<*, *, THIRD, *>.thirdOrElse(get: () -> ELSE): R =
    if (isThird) value as THIRD as R
    else get() as R

inline fun <FOURTH : R, ELSE : R, R> Either4<*, *, *, FOURTH>.fourthOrElse(get: () -> ELSE): R =
    if (isFourth) value as FOURTH as R
    else get() as R
