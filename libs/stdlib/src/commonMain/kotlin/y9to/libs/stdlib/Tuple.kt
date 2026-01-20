@file:Suppress("NOTHING_TO_INLINE")

package y9to.libs.stdlib


data object Tuple0 {
    override fun toString() = "()"
}

data class Tuple1<A>(val first: A) {
    override fun toString(): String = "($first)"
}

typealias Tuple2<A, B> = Pair<A, B>

typealias Tuple3<A, B, C> = Triple<A, B, C>

data class Tuple4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

data class Tuple5<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
) {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

data class Tuple6<out A, out B, out C, out D, out E, out F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
) {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth)"
}

data class Tuple7<out A, out B, out C, out D, out E, out F, out G>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G
) {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh)"
}

data class Tuple8<out A, out B, out C, out D, out E, out F, out G, out H>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H
) {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh, $eighth)"
}

data class Tuple9<out A, out B, out C, out D, out E, out F, out G, out H, out I>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
    val ninth: I
) {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh, $eighth, $ninth)"
}

data class Tuple10<out A, out B, out C, out D, out E, out F, out G, out H, out I, out J>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
    val ninth: I,
    val tenth: J
) {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh, $eighth, $ninth, $tenth)"
}

inline fun tupleOf() = Tuple0
inline fun <A> tupleOf(first: A) = Tuple1(first)
inline fun <A, B> tupleOf(first: A, second: B) = Tuple2(first, second)
inline fun <A, B, C> tupleOf(first: A, second: B, third: C) = Tuple3(first, second, third)
inline fun <A, B, C, D> tupleOf(first: A, second: B, third: C, fourth: D) = Tuple4(first, second, third, fourth)
inline fun <A, B, C, D, E> tupleOf(first: A, second: B, third: C, fourth: D, fifth: E) = Tuple5(first, second, third, fourth, fifth)
inline fun <A, B, C, D, E, F> tupleOf(first: A, second: B, third: C, fourth: D, fifth: E, sixth: F) = Tuple6(first, second, third, fourth, fifth, sixth)
inline fun <A, B, C, D, E, F, G> tupleOf(first: A, second: B, third: C, fourth: D, fifth: E, sixth: F, seventh: G) = Tuple7(first, second, third, fourth, fifth, sixth, seventh)
inline fun <A, B, C, D, E, F, G, H> tupleOf(first: A, second: B, third: C, fourth: D, fifth: E, sixth: F, seventh: G, eighth: H) = Tuple8(first, second, third, fourth, fifth, sixth, seventh, eighth)
inline fun <A, B, C, D, E, F, G, H, I> tupleOf(first: A, second: B, third: C, fourth: D, fifth: E, sixth: F, seventh: G, eighth: H, ninth: I) = Tuple9(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth)
inline fun <A, B, C, D, E, F, G, H, I, J> tupleOf(first: A, second: B, third: C, fourth: D, fifth: E, sixth: F, seventh: G, eighth: H, ninth: I, tenth: J) = Tuple10(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth)
