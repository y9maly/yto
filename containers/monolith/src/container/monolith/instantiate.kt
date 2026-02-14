@file:Suppress("RedundantSuspendModifier")

package container.monolith

import presentation.authenticator.SillyAuthenticator
import kotlin.time.Clock


suspend fun instantiate(
    postgresUrl: String = MonolithDefaults.postgresUrl,
    fileGatewayAddress: String = MonolithDefaults.fileGatewayAddress,
    filesDirectory: String = MonolithDefaults.filesDirectory,
): Monolith {
    val database = createDatabase(url = postgresUrl)
    val repository = createRepository(
        database = database,
    )

    val fileStorage = createFileStorage(filesDirectory)
    val service = createService(
        repository = repository,
        selector = createSelector(repository),
        clock = Clock.System,
        fileStorage = fileStorage,
    )

    val assembler = createAssembler(service)
    val presenter = createPresenter(service)
    val controller = createController(
        fileGatewayAddress = fileGatewayAddress,
        uploadFilePath = "file/upload",
        downloadFilePath = "file/download",
        service = service,
        assembler = assembler,
        presenter = presenter,
    )

    val rpc = createRpc(SillyAuthenticator(), controller)

    return Monolith(
        database = database,
        repository = repository,
        fileStorage = fileStorage,
        service = service,
        presenter = presenter,
        assembler = assembler,
        controller = controller,
        rpc = rpc,
    )
}
