package presentation.assembler

import backend.core.types.UserId
import domain.service.ServiceCollection
import presentation.integration.context.Context
import presentation.mapper.map
import y9to.api.types.InputUser


class UserAssemblerImpl(
    private val service: ServiceCollection,
) : UserAssembler {
    context(context: Context)
    override suspend fun resolve(input: InputUser): UserId? {
        when (input) {
            is InputUser.Id -> {
                return input.id.map()
            }
        }
    }

    context(context: Context)
    override suspend fun UserId(id: y9to.api.types.UserId): UserId {
        return id.map()
    }
}
