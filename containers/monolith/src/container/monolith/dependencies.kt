package container.monolith

import domain.service.*
import integration.eventCollector.EventCollector
import integration.fileStorage.FileStorage
import integration.fileStorage.LocalFileStorage
import integration.repository.PostgresRepositoryCollection
import integration.repository.RepositoryCollection
import io.lettuce.core.RedisClient
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
import presentation.api.krpc.AuthRpcDefault
import presentation.api.krpc.FileRpcDefault
import presentation.api.krpc.PostRpcDefault
import presentation.api.krpc.UpdateRpcDefault
import presentation.api.krpc.UserRpcDefault
import presentation.assembler.AssemblerCollection
import presentation.assembler.FileAssemblerImpl
import presentation.assembler.PostAssemblerImpl
import presentation.assembler.UserAssemblerImpl
import presentation.authenticator.Authenticator
import presentation.presenter.*
import presentation.tokenProvider.TokenProvider
import presentation.updateProvider.UpdateProvider
import presentation.updateSubscriptionsStore.UpdateSubscriptionsStore
import y9to.api.controller.*
import y9to.api.krpc.RpcCollection
import y9to.api.types.FileSink
import y9to.api.types.FileSource
import kotlin.io.encoding.Base64
import kotlin.io.path.Path
import kotlin.properties.Delegates
import kotlin.time.Clock


internal fun createRpc(
    authenticator: Authenticator,
    tokenProvider: TokenProvider,
    controller: ControllerCollection
): RpcCollection {
    return RpcCollection(
        auth = AuthRpcDefault(authenticator, tokenProvider, controller.auth),
        user = UserRpcDefault(authenticator, controller.user),
        post = PostRpcDefault(authenticator, controller.post),
        file = FileRpcDefault(authenticator, controller.file),
        update = UpdateRpcDefault(authenticator, controller.update),
    )
}

internal fun createController(
    fileGatewayAddress: String, //   https://example.com
    uploadFilePath: String,     //   file/upload
    downloadFilePath: String,   //   file/download
    loginService: LoginService,
    service: ServiceCollection,
    assembler: AssemblerCollection,
    presenter: PresenterCollection,
    updateProvider: UpdateProvider,
    updateSubscriptionsStore: UpdateSubscriptionsStore,
): ControllerCollection {
    return ControllerCollection(
        auth = AuthControllerDefault(loginService, service, assembler, presenter),
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
        update = UpdateControllerDefault(assembler, presenter, updateProvider, updateSubscriptionsStore)
    )
}

internal fun createPresenter(service: ServiceCollection): PresenterCollection {
    var presenterCollection by Delegates.notNull<PresenterCollection>()
    presenterCollection = PresenterCollection(
        auth = AuthPresenterImpl(service),
        user = UserPresenterImpl(lazy { presenterCollection }, service),
        post = PostPresenterImpl(service),
        file = FilePresenterImpl(),
    )
    return presenterCollection
}

internal fun createAssembler(service: ServiceCollection): AssemblerCollection {
    return AssemblerCollection(
        user = UserAssemblerImpl(service),
        post = PostAssemblerImpl(service),
        file = FileAssemblerImpl(),
    )
}

internal fun createService(
    repository: RepositoryCollection,
    eventCollector: EventCollector,
    fileStorage: FileStorage,
    clock: Clock,
): ServiceCollection {
    return ServiceCollection(
        auth = AuthServiceImpl(repository, eventCollector, clock),
        user = UserServiceImpl(repository, eventCollector, clock),
        post = PostServiceImpl(repository, eventCollector, clock),
        file = FileServiceImpl(repository, fileStorage, clock),
    )
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
internal fun createRepository(database: R2dbcDatabase): RepositoryCollection {
    return PostgresRepositoryCollection(
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

internal fun createRedisClient(url: String): RedisClient {
    return RedisClient.create(url)
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
