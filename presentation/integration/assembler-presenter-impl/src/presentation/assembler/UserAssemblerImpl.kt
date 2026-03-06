package presentation.assembler

import backend.core.types.UserLink
import backend.core.types.UserId
import domain.service.MainService
import presentation.integration.context.Context
import presentation.mapper.map
import y9to.api.types.InputUser


class UserAssemblerImpl(
    private val service: MainService,
) : UserAssembler {
    context(context: Context)
    override suspend fun resolve(input: InputUser): UserLink? {
        when (input) {
            is InputUser.Id -> {
                return UserLink.Id(input.id.map())
            }
        }
    }

    context(context: Context)
    override suspend fun UserId(id: y9to.api.types.UserId): UserId {
        return id.map()
    }
}
