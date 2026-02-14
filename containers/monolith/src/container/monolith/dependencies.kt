package container.monolith

import domain.selector.MainSelector
import domain.service.AuthServiceImpl
import domain.service.FileServiceImpl
import domain.service.MainService
import domain.service.PostServiceImpl
import domain.service.UserServiceImpl
import integration.fileStorage.FileStorage
import integration.fileStorage.LocalFileStorage
import integration.repository.MainRepository
import integration.repository.MainRepositoryPostgres
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig.Companion.invoke
import presentation.api.krpc.AuthRpcDefault
import presentation.api.krpc.FileRpcDefault
import presentation.api.krpc.PostRpcDefault
import presentation.api.krpc.UserRpcDefault
import presentation.assembler.FileAssembler
import presentation.assembler.FileAssemblerImpl
import presentation.assembler.MainAssembler
import presentation.assembler.PostAssemblerImpl
import presentation.assembler.UserAssemblerImpl
import presentation.authenticator.Authenticator
import presentation.presenter.AuthPresenterImpl
import presentation.presenter.FilePresenterImpl
import presentation.presenter.MainPresenter
import presentation.presenter.PostPresenterImpl
import presentation.presenter.UserPresenterImpl
import y9to.api.controller.AuthControllerDefault
import y9to.api.controller.FileControllerDefault
import y9to.api.controller.MainController
import y9to.api.controller.PostControllerDefault
import y9to.api.controller.UserControllerDefault
import y9to.api.krpc.MainRpc
import y9to.api.types.FileSink
import y9to.api.types.FileSource
import kotlin.io.encoding.Base64
import kotlin.io.path.Path
import kotlin.properties.Delegates
import kotlin.time.Clock


internal fun createRpc(
    authenticator: Authenticator,
    controller: MainController
): MainRpc {
    return MainRpc(
        auth = AuthRpcDefault(authenticator, controller.auth),
        user = UserRpcDefault(authenticator, controller.user),
        post = PostRpcDefault(authenticator, controller.post),
        file = FileRpcDefault(authenticator, controller.file)
    )
}

internal fun createController(
    fileGatewayAddress: String, //   https://example.com
    uploadFilePath: String,     //   file/upload
    downloadFilePath: String,   //   file/download
    service: MainService,
    assembler: MainAssembler,
    presenter: MainPresenter,
): MainController {
    return MainController(
        auth = AuthControllerDefault(service, assembler, presenter),
        user = UserControllerDefault(service, assembler, presenter),
        post = PostControllerDefault(service, assembler, presenter),
        file = FileControllerDefault(service, assembler, presenter,
            fileSink = { uri ->
                val uriBase64 = Base64.UrlSafe
                    .encode(uri.toByteArray(Charsets.UTF_8))
                    .replace('=', '_')
                val url = "$fileGatewayAddress/$uploadFilePath/$uriBase64"
                FileSink.HttpOctetStream(url)
            },
            fileSource = { uri ->
                val uriBase64 = Base64.UrlSafe
                    .encode(uri.toByteArray(Charsets.UTF_8))
                    .replace('=', '_')
                val url = "$fileGatewayAddress/$downloadFilePath/$uriBase64"
                FileSource.HttpOctetStream(url)
            }
        ),
    )
}

internal fun createPresenter(service: MainService): MainPresenter {
    var mainPresenter by Delegates.notNull<MainPresenter>()
    mainPresenter = MainPresenter(
        auth = AuthPresenterImpl(service),
        user = UserPresenterImpl(lazy { mainPresenter }, service),
        post = PostPresenterImpl(service),
        file = FilePresenterImpl(),
    )
    return mainPresenter
}

internal fun createAssembler(service: MainService): MainAssembler {
    return MainAssembler(
        user = UserAssemblerImpl(service),
        post = PostAssemblerImpl(service),
        file = FileAssemblerImpl(),
    )
}

internal fun createService(
    repository: MainRepository,
    selector: MainSelector,
    fileStorage: FileStorage,
    clock: Clock,
): MainService {
    return MainService(
        auth = AuthServiceImpl(repository, clock),
        user = UserServiceImpl(repository, selector, clock),
        post = PostServiceImpl(repository, selector, clock),
        file = FileServiceImpl(repository, fileStorage, clock),
    )
}

internal fun createSelector(repository: MainRepository): MainSelector {
    return MainSelector(repository)
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
internal fun createRepository(database: R2dbcDatabase): MainRepository {
    return MainRepositoryPostgres(
        database = database,
        transactionCoroutineContext = newSingleThreadContext("Repository-Database-Thread"),
    )
}

internal fun createFileStorage(directory: String): FileStorage {
    return LocalFileStorage(
        blockingContext = Dispatchers.IO,
        directory = Path(directory),
    )
}

internal fun createDatabase(url: String): R2dbcDatabase {
    return R2dbcDatabase.connect(
        url = url,
        driver = "postgresql",
        databaseConfig = R2dbcDatabaseConfig {
            useNestedTransactions = true
            explicitDialect = PostgreSQLDialect()
            sqlLogger = object : SqlLogger {
                override fun log(context: StatementContext, transaction: Transaction) {
//                    println(context.expandArgs(transaction))
                }
            }
        }
    )
}
