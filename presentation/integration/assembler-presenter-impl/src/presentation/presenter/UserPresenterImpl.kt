package presentation.presenter

import domain.service.MainService
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.authStateOrPut
import presentation.integration.callContext.elements.sessionId
import presentation.mapper.map
import y9to.api.types.MyProfile
import y9to.api.types.User


class UserPresenterImpl(
    private val service: MainService,
) : UserPresenter {
    context(callContext: CallContext)
    override suspend fun User(backendUser: backend.core.types.User): User {
        val authState = callContext.authStateOrPut {
            service.auth.getAuthState(callContext.sessionId)
                ?: error("Unauthenticated")
        }

        val isSelf = authState.userIdOrNull() == backendUser.id

        return backendUser.map(
            showPhoneNumber = isSelf,
            showEmail = true,
        )
    }

    override suspend fun MyProfile(backendUser: backend.core.types.User): MyProfile {
        return MyProfile(
            id = backendUser.id.map(),
            registrationDate = backendUser.registrationDate,
            firstName = backendUser.firstName,
            lastName = backendUser.lastName,
            phoneNumber = backendUser.phoneNumber,
            email = backendUser.email,
            bio = backendUser.bio,
            birthday = backendUser.birthday,
        )
    }
}
