package presentation.api.krpc

import backend.core.reference.UserReference
import domain.service.MainService
import domain.service.result.EditUserError
import presentation.api.krpc.internals.authenticate
import presentation.assembler.MainAssembler
import presentation.authenticator.Authenticator
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.authStateOrPut
import presentation.integration.callContext.elements.sessionId
import presentation.presenter.MainPresenter
import y9to.api.krpc.UserRpc
import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.optional.Optional


class UserRpcImpl(
    private val authenticator: Authenticator,
    private val service: MainService,
    private val assembler: MainAssembler,
    private val presenter: MainPresenter,
) : UserRpc {
    override suspend fun getMyProfile(token: Token): MyProfile? = authenticate(token) {
        val userId = authStateOrPut {
            service.auth.getAuthState(sessionId) ?: return@authenticate null
        }.userIdOrNull() ?: return@authenticate null
        val user = service.user.get(userId) ?: return@authenticate null
        presenter.user.MyProfile(user)
    }

    override suspend fun get(token: Token, input: InputUser): User? = authenticate(token) {
        val userRef = assembler.user.resolve(input)
            ?: return null
        val user = service.user.get(userRef)
            ?: return null
        val remoteUser = presenter.user.User(user)
        remoteUser
    }

    override suspend fun editMe(
        token: Token,
        firstName: Optional<String>,
        lastName: Optional<String?>,
        bio: Optional<String?>,
        birthday: Optional<Birthday?>
    ): EditMeResult = authenticate(token) {
        val userId = authStateOrPut {
            service.auth.getAuthState(sessionId) ?: return@authenticate EditMeError.Unauthenticated.asError()
        }.userIdOrNull() ?: return@authenticate EditMeError.Unauthenticated.asError()

        if (listOf(
            firstName,
            lastName,
            bio,
            birthday,
        ).any { it.isPresent }) {
            service.user.edit(
                ref = UserReference.Id(userId),
                firstName = firstName,
                lastName = lastName,
                bio = bio,
                birthday = birthday,
            ).onError { error ->
                when (error) {
                    EditUserError.UnknownUserReference -> error("Unreachable")
                }
            }
        }

        val newUser = service.user.get(userId) ?: error("Unreachable")
        presenter.user.MyProfile(newUser).asOk()
    }

    private suspend inline fun <R> authenticate(token: Token, block: CallContext.() -> R) =
        authenticate(authenticator, token, block)
}
