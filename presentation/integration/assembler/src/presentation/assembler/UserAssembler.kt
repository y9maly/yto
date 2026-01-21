@file:Suppress("RedundantSuspendModifier")

package presentation.assembler

import backend.core.reference.UserReference
import presentation.integration.callContext.CallContext
import y9to.api.types.InputUser


interface UserAssembler {
    context(callContext: CallContext)
    suspend fun resolve(input: InputUser): UserReference?
}
