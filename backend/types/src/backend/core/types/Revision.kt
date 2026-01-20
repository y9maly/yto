package backend.core.types


@JvmInline
value class Revision(val long: Long) {
    operator fun compareTo(other: Revision): Int = long.compareTo(other.long)
}
