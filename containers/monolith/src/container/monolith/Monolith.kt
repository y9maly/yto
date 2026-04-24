package container.monolith

import domain.service.ServiceCollection
import integration.fileStorage.FileStorage
import integration.repository.RepositoryCollection
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import presentation.assembler.AssemblerCollection
import presentation.authenticator.Authenticator
import presentation.infra.jwtManager.JwtManager
import presentation.infra.updateManager.UpdateManager
import presentation.presenter.PresenterCollection
import y9to.api.controller.ControllerCollection
import y9to.api.krpc.RpcCollection


data class Monolith(
    val database: R2dbcDatabase,
    val repository: RepositoryCollection,
    val fileStorage: FileStorage,
    val service: ServiceCollection,
    val presenter: PresenterCollection,
    val assembler: AssemblerCollection,
    val controller: ControllerCollection,
    val updateManager: UpdateManager,
    val jwtManager: JwtManager,
    val authenticator: Authenticator,
    val rpc: RpcCollection,
)
