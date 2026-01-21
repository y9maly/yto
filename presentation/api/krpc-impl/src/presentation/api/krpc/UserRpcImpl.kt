package presentation.api.krpc

import domain.service.MainService
import presentation.api.krpc.internals.authenticate
import presentation.assembler.MainAssembler
import presentation.assembler.UserAssembler
import presentation.authenticator.Authenticator
import presentation.integration.callContext.CallContext
import presentation.presenter.MainPresenter
import y9to.api.krpc.UserRpc
import y9to.api.types.InputUser
import y9to.api.types.Token
import y9to.api.types.User


class UserRpcImpl(
    private val authenticator: Authenticator,
    private val service: MainService,
    private val assembler: MainAssembler,
    private val presenter: MainPresenter,
) : UserRpc {
    override suspend fun get(token: Token, input: InputUser): User? = authenticate(token) {
        val userRef = assembler.user.resolve(input)
            ?: return null
        val user = service.user.get(userRef)
            ?: return null
        val remoteUser = presenter.user.User(user)
        remoteUser
    }

    private suspend inline fun <R> authenticate(token: Token, block: CallContext.() -> R) =
        authenticate(authenticator, token, block)
}
