package presentation.assembler

import backend.core.reference.UserReference
import backend.core.types.UserId
import domain.service.MainService
import presentation.integration.callContext.CallContext
import presentation.mapper.map
import y9to.api.types.InputUser


class UserAssemblerImpl(
    private val service: MainService,
) : UserAssembler {
    context(callContext: CallContext)
    override suspend fun resolve(input: InputUser): UserReference? {
        when (input) {
            is InputUser.Id -> {
                return UserReference.Id(input.id.map())
            }
        }
    }

    context(callContext: CallContext)
    override suspend fun UserId(id: y9to.api.types.UserId): UserId {
        return id.map()
    }
}
