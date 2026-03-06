package y9to.libs.stdlib.multiplatform

import kotlin.reflect.KClass


/**
 * @return [KClass.qualifiedName] if available on current platform.
 * Currently, it returns null on Web (js and wasm) targets.
 */
internal expect inline fun KClass<*>.qualifiedNameOrNull(): String?
