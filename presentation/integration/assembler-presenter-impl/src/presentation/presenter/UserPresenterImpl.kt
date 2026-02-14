package presentation.presenter

import domain.service.MainService
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.mapper.map
import y9to.api.types.MyProfile
import y9to.api.types.User


class UserPresenterImpl(
    main: Lazy<MainPresenter>,
    private val service: MainService,
) : UserPresenter {
    private val main by main

    context(context: Context)
    override suspend fun User(backendUser: backend.core.types.User): User {
        val authState = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: error("Unauthenticated")
        }

        val isSelf = authState.userIdOrNull() == backendUser.id

        return backendUser.map(
            showPhoneNumber = isSelf,
            showEmail = true,
        )
    }

    context(context: Context)
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
            cover = backendUser.cover?.let { main.file.FileId(it) },
            avatar = backendUser.avatar?.let { main.file.FileId(it) },
        )
    }
}
