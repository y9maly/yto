package integration.repository.internals

import org.jetbrains.exposed.v1.core.CustomFunction
import org.jetbrains.exposed.v1.core.DoubleColumnType


internal class RandomFunction : CustomFunction<Double>(
    functionName = "RANDOM",
    columnType = DoubleColumnType(),
    expr = arrayOf()
)
