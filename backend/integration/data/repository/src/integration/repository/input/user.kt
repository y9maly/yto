@file:Suppress("FunctionName")

package integration.repository.input

import backend.core.types.Filter
import backend.core.types.UserId


typealias UserFilter = Filter<UserPredicate>
sealed interface UserPredicate {
    data class Id(val id: UserId) : UserPredicate
    data class Ids(val ids: Set<UserId>) : UserPredicate
}
