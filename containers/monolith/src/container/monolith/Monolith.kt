package container.monolith

import domain.service.MainService
import integration.fileStorage.FileStorage
import integration.repository.MainRepository
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import presentation.assembler.MainAssembler
import presentation.presenter.MainPresenter
import y9to.api.controller.MainController
import y9to.api.krpc.MainRpc


data class Monolith(
    val database: R2dbcDatabase,
    val repository: MainRepository,
    val fileStorage: FileStorage,
    val service: MainService,
    val presenter: MainPresenter,
    val assembler: MainAssembler,
    val controller: MainController,
    val rpc: MainRpc,
)
