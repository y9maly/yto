package backend.core.types

import kotlinx.serialization.Serializable as S


@RequiresOptIn("This api will be removed in the future")
annotation class TemporaryRevisionApi

@OptIn(TemporaryRevisionApi::class)
@S data class Revision(
    @property:TemporaryRevisionApi
    val long: Long
) : Comparable<Revision> {
    override operator fun compareTo(other: Revision): Int = long.compareTo(other.long)
    override fun toString() = "Revision($long)"
}
