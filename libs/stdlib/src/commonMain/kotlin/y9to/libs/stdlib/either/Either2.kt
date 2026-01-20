// USELESS_CAST нужен из-за иногда возникающих ошибок
// actual type is T, but T was expected
@file:Suppress("NOTHING_TO_INLINE", "unused", "UNCHECKED_CAST", "USELESS_CAST")

package y9to.libs.stdlib.either

import kotlin.jvm.JvmInline


@JvmInline
value class Either2<out FIRST, out SECOND>(@PublishedApi internal val value: Any?) {
    companion object {
        inline fun <VALUE> first(value: VALUE) = Either2<VALUE, Nothing>(value)
        inline fun <VALUE> second(value: VALUE) = Either2<Nothing, VALUE>(Second(value))
    }

    @PublishedApi
    internal class Second(val value: Any?)

    inline val isFirst: Boolean get() = value !is Second
    inline val isSecond: Boolean get() = value is Second

    override fun toString() =
        if (isFirst) "Either2.First($value)"
        else "Either2.Second($value)"
}


inline fun <FIRST> Either2<FIRST, *>.asFirst(): FIRST =
    if (isFirst) value as FIRST
    else throw EitherCastException()

inline fun <SECOND> Either2<*, SECOND>.asSecond(): SECOND =
    if (isSecond) value as SECOND
    else throw EitherCastException()


inline fun <FIRST> Either2<FIRST, *>.firstOrNull(): FIRST? =
    if (isFirst) value as FIRST
    else null

inline fun <SECOND> Either2<*, SECOND>.secondOrNull(): SECOND? =
    if (isSecond) value as SECOND
    else null


inline fun <FIRST> Either2<FIRST, *>.firstOrDefault(get: () -> FIRST): FIRST =
    if (isFirst) value as FIRST
    else get()

inline fun <SECOND> Either2<*, SECOND>.secondOrDefault(get: () -> SECOND): SECOND =
    if (isSecond) value as SECOND
    else get()


inline fun <FIRST : R, ELSE : R, R> Either2<FIRST, *>.firstOrElse(get: () -> ELSE): R =
    if (isFirst) value as FIRST as R
    else get() as R

inline fun <SECOND : R, ELSE : R, R> Either2<*, SECOND>.secondOrElse(get: () -> ELSE): R =
    if (isSecond) value as SECOND as R
    else get() as R


inline fun <FIRST> Either2<FIRST, *>.onFirst(block: (FIRST) -> Unit) =
    if (isFirst) block(value as FIRST)
    else Unit

inline fun <SECOND> Either2<*, SECOND>.onSecond(block: (SECOND) -> Unit) =
    if (isSecond) block(value as SECOND)
    else Unit


inline fun <OLD, NEW, SECOND> Either2<OLD, SECOND>.mapFirst(transform: (OLD) -> NEW): Either2<NEW, SECOND> =
    if (isFirst) Either2.first(transform(value as OLD))
    else this as Either2<NEW, SECOND>

inline fun <FIRST, OLD, NEW> Either2<FIRST, OLD>.mapSecond(transform: (OLD) -> NEW): Either2<FIRST, NEW> =
    if (isFirst) this as Either2<FIRST, NEW>
    else Either2.second(transform(value as OLD))
