package y9to.common.types

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month


data class Birthday(
    val year: Int?,
    val month: Month,
    val dayOfMonth: Int,
) {
    fun toLocalDate(defaultYear: Int): LocalDate {
        return LocalDate(year ?: defaultYear, month, dayOfMonth)
    }
}
