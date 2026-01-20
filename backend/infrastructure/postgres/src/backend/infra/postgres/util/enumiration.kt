package backend.infra.postgres.util

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.Table
import kotlin.enums.enumEntries


internal inline fun <reified T : Enum<T>> Table.enumerationBy(
    name: String,
    sqlEnum: String?,
    crossinline selector: (T) -> Any
): Column<T> {
    val map by lazy { enumEntries<T>().associateBy(selector) }

    val columnType = object : IColumnType<T> {
        override var nullable = false

        override fun sqlType() = error("No sqlType")

        override fun valueFromDB(value: Any): T {
            return map[value] ?: error("$value can't be associated with any from enum ${T::class.qualifiedName}")
        }

        override fun notNullValueToDB(value: T): Any {
            return selector(value)
        }

        override fun parameterMarker(value: T?): String {
            if (sqlEnum == null)
                return super.parameterMarker(value)
            return "${super.parameterMarker(value)}::$sqlEnum"
        }
    }

    return registerColumn(name, columnType)
}
