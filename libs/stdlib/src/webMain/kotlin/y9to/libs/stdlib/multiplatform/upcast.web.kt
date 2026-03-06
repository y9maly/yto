@file:Suppress("NOTHING_TO_INLINE")

package y9to.libs.stdlib.multiplatform

import kotlin.reflect.KClass


internal actual inline fun KClass<*>.qualifiedNameOrNull(): String? {
    return null
}
