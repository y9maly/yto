// USELESS_CAST нужен из-за иногда возникающих ошибок
// actual type is T, but T was expected
@file:Suppress("NOTHING_TO_INLINE", "unused", "UNCHECKED_CAST", "USELESS_CAST")

package y9to.libs.stdlib.either

import kotlin.jvm.JvmInline


@PublishedApi internal const val Either3_VARIANT_SECOND = false
@PublishedApi internal const val Either3_VARIANT_THIRD = true

@JvmInline
value class Either3<out FIRST, out SECOND, out THIRD>(@PublishedApi internal val value: Any?) {
    companion object {
        inline fun <VALUE> first(value: VALUE) = Either3<VALUE, Nothing, Nothing>(value)
        inline fun <VALUE> second(value: VALUE) = Either3<Nothing, VALUE, Nothing>(Other(Either3_VARIANT_SECOND, value))
        inline fun <VALUE> third(value: VALUE) = Either3<Nothing, Nothing, VALUE>(Other(Either3_VARIANT_THIRD, value))
    }

    @PublishedApi
    internal class Other(val variant: Boolean, val value: Any?)

    inline val isFirst: Boolean get() = value !is Other
    inline val isSecond: Boolean get() = value is Other && value.variant == Either3_VARIANT_SECOND
    inline val isThird: Boolean get() = value is Other && value.variant == Either3_VARIANT_THIRD

    override fun toString() =
        if (isFirst) "Either3.First($value)"
        else if (isSecond) "Either3.Second($value)"
        else "Either3.Third($value)"
}

inline fun <FIRST> Either3<FIRST, *, *>.asFirst(): FIRST =
    if (isFirst) value as FIRST
    else throw EitherCastException()

inline fun <SECOND> Either3<*, SECOND, *>.asSecond(): SECOND =
    if (isSecond) value as SECOND
    else throw EitherCastException()

inline fun <THIRD> Either3<*, *, THIRD>.asThird(): THIRD =
    if (isThird) value as THIRD
    else throw EitherCastException()


inline fun <FIRST> Either3<FIRST, *, *>.firstOrNull(): FIRST? =
    if (isFirst) value as FIRST
    else null

inline fun <SECOND> Either3<*, SECOND, *>.secondOrNull(): SECOND? =
    if (isSecond) value as SECOND
    else null

inline fun <THIRD> Either3<*, *, THIRD>.thirdOrNull(): THIRD? =
    if (isThird) value as THIRD
    else null


inline fun <FIRST> Either3<FIRST, *, *>.firstOrDefault(get: () -> FIRST): FIRST =
    if (isFirst) value as FIRST
    else get()

inline fun <SECOND> Either3<*, SECOND, *>.secondOrDefault(get: () -> SECOND): SECOND =
    if (isSecond) value as SECOND
    else get()

inline fun <THIRD> Either3<*, *, THIRD>.thirdOrDefault(get: () -> THIRD): THIRD =
    if (isThird) value as THIRD
    else get()


inline fun <FIRST : R, ELSE : R, R> Either3<FIRST, *, *>.firstOrElse(get: () -> ELSE): R =
    if (isFirst) value as FIRST as R
    else get() as R

inline fun <SECOND : R, ELSE : R, R> Either3<*, SECOND, *>.secondOrElse(get: () -> ELSE): R =
    if (isSecond) value as SECOND as R
    else get() as R

inline fun <THIRD : R, ELSE : R, R> Either3<*, *, THIRD>.thirdOrElse(get: () -> ELSE): R =
    if (isThird) value as THIRD as R
    else get() as R
