@file:JvmName("ResultUserKt")

package y9to.api.types

import y9to.libs.stdlib.Union
import kotlin.jvm.JvmName


typealias EditMeResult = Union<EditMeOk, EditMeError>

typealias EditMeOk = MyProfile

sealed interface EditMeError {
    data object Unauthenticated : EditMeError
}
