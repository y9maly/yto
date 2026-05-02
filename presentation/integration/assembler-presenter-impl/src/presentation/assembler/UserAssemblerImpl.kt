package presentation.assembler

import backend.core.types.UserId
import domain.service.ServiceCollection
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.mapper.map
import y9to.api.types.InputUser


class UserAssemblerImpl(
    private val service: ServiceCollection,
) : UserAssembler {
    context(context: Context)
    override suspend fun resolve(input: InputUser): UserId? {
        when (input) {
            is y9to.api.types.UserId -> {
                return input.map()
            }

            is InputUser.Me -> {
                val authState = authStateOrPut {
                    service.auth.getAuthState(sessionId)
                        ?: return null
                }

                return authState.userIdOrNull()
            }
        }
    }

    context(context: Context)
    override suspend fun UserId(id: y9to.api.types.UserId): UserId {
        return id.map()
    }
}
