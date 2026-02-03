package play.api

import play.service.service
import presentation.api.krpc.AuthRpcImpl
import presentation.api.krpc.PostRpcImpl
import presentation.api.krpc.UserRpcImpl
import presentation.assembler.MainAssembler
import presentation.assembler.PostAssemblerImpl
import presentation.assembler.UserAssemblerImpl
import presentation.authenticator.SillyAuthenticator
import presentation.presenter.AuthPresenterImpl
import presentation.presenter.MainPresenter
import presentation.presenter.PostPresenterImpl
import presentation.presenter.UserPresenterImpl
import y9to.api.krpc.MainRpc


val authenticator = SillyAuthenticator()

val assembler = MainAssembler(
    user = UserAssemblerImpl(service),
    post = PostAssemblerImpl(service),
    TODO()
)

val presenter = MainPresenter(
    auth = AuthPresenterImpl(service),
    user = UserPresenterImpl(service),
    post = PostPresenterImpl(service),
    TODO()
)

val rpc = MainRpc(
    auth = AuthRpcImpl(authenticator, service, presenter),
    user = UserRpcImpl(authenticator, service, assembler, presenter),
    post = PostRpcImpl(authenticator, service, assembler, presenter),
    TODO()
)
