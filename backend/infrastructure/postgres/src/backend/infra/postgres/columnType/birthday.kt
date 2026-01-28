package backend.infra.postgres.columnType

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import y9to.common.types.Birthday
import kotlin.time.Instant


private const val FIRST_LEAP_YEAR = 0x0004

private class BirthdayColumnType : ColumnType<Birthday>() {
    override fun sqlType() = currentDialect.dataTypeProvider.dateType()

    private fun localDateValueFromDB(value: Any): LocalDate = when (value) {
        is LocalDate -> value
        is java.sql.Date -> longToLocalDate(value.time)
        is java.sql.Timestamp -> longToLocalDate(value.time)
        is Int -> longToLocalDate(value.toLong())
        is Long -> longToLocalDate(value)
        is String -> LocalDate.parse(value)
        else -> LocalDate.parse(value.toString())
    }

    private fun longToLocalDate(epochMillis: Long): LocalDate {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        return instant.toLocalDateTime(TimeZone.UTC).date
    }

    override fun valueFromDB(value: Any): Birthday {
        val date = localDateValueFromDB(value)
        if (date.year == FIRST_LEAP_YEAR)
            return Birthday(null, date.month, date.day)
        return Birthday(date.year, date.month, date.day)
    }

    // java.sql.Date(-30589334220000) говорит, что это 24 авгута 1000 года.
    // Но это 30 августа (точно точно).
    // java.sql.Date как раз таки в exposed юзается.
    // Кароче дефолтно в exposed старое апи юзается и вообще капец оно всё ломает капец блин.
    // Пришлось свое писать.
    override fun notNullValueToDB(value: Birthday): Any {
        return java.time.LocalDate.of(
            value.year ?: FIRST_LEAP_YEAR,
            value.month.number,
            value.dayOfMonth
        )
    }
}

fun Table.birthday(name: String): Column<Birthday> = registerColumn(name, BirthdayColumnType())
