@file:Suppress("RedundantSuspendModifier")

package presentation.assembler

import backend.core.reference.UserReference
import presentation.integration.callContext.CallContext
import y9to.api.types.InputUser
import y9to.api.types.UserId
import backend.core.types.UserId as BackendUserId


interface UserAssembler {
    context(callContext: CallContext)
    suspend fun resolve(input: InputUser): UserReference?

    context(callContext: CallContext)
    suspend fun UserId(id: UserId): BackendUserId
}
