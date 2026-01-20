package backend.infra.postgres.columnType

import y9to.common.types.Birthday
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.date


private const val FIRST_LEAP_YEAR = 0x0004

fun Table.birthday(name: String): Column<Birthday> = date(name)
    .transform(
        wrap = {
            if (it.year == FIRST_LEAP_YEAR)
                return@transform Birthday(null, it.month, it.day)
            Birthday(it.year, it.month, it.day)
        },
        unwrap = { it.toLocalDate(FIRST_LEAP_YEAR) }
    )
