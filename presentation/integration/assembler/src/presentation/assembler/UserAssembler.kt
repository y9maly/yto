@file:Suppress("RedundantSuspendModifier")

package presentation.assembler

import backend.core.types.UserReference
import presentation.integration.context.Context
import y9to.api.types.InputUser
import y9to.api.types.UserId
import backend.core.types.UserId as BackendUserId


interface UserAssembler {
    context(context: Context)
    suspend fun resolve(input: InputUser): UserReference?

    context(context: Context)
    suspend fun UserId(id: UserId): BackendUserId
}

context(_: Context, assembler: UserAssembler)
suspend fun InputUser.resolve(): UserReference? = assembler.resolve(this)

context(_: Context, assembler: UserAssembler)
suspend fun UserId.map(): BackendUserId = assembler.UserId(this)
