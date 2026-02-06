@file:Suppress("SameParameterValue")

package container.monolith

import backend.core.types.FileId
import domain.selector.MainSelector
import domain.service.*
import domain.service.result.CommitFilePartsResult
import domain.service.result.UploadFilePartResult
import integration.fileStorage.FileStorage
import integration.fileStorage.LocalFileStorage
import integration.repository.MainRepository
import integration.repository.MainRepositoryPostgres
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.UnsafeIoApi
import kotlinx.io.unsafe.UnsafeBufferOperations
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import presentation.api.krpc.AuthRpcImpl
import presentation.api.krpc.FileRpcImpl
import presentation.api.krpc.PostRpcImpl
import presentation.api.krpc.UserRpcImpl
import presentation.assembler.FileAssemblerImpl
import presentation.assembler.MainAssembler
import presentation.assembler.PostAssemblerImpl
import presentation.assembler.UserAssemblerImpl
import presentation.authenticator.Authenticator
import presentation.authenticator.SillyAuthenticator
import presentation.gateway.ktorKrpc.krpcApiModule
import presentation.presenter.*
import y9to.api.krpc.MainRpc
import y9to.api.krpc.types.FileSink
import y9to.api.krpc.types.FileSource
import kotlin.io.encoding.Base64
import kotlin.io.path.Path
import kotlin.properties.Delegates
import kotlin.text.Charsets
import kotlin.text.replace
import kotlin.text.toByteArray
import kotlin.text.toInt
import kotlin.text.toLong
import kotlin.text.toLongOrNull
import kotlin.time.Clock


fun main() {
    val host = System.getenv("ktor_host") ?: "0.0.0.0"
    val port = System.getenv("ktor_port")?.toInt() ?: 8103
    // r2dbc:postgresql://user:password@host:port/database
    val postgresUrl = System.getenv("postgres_url") ?: error("postgres_url environment variable is required")
    val fileGatewayAddress = System.getenv("ktor_file_gateway_address") ?: "http://localhost:$port"
    // /home/user/server/files
    val filesDirectory = System.getenv("y9to_files_directory") ?: error("y9to_files_directory environment variable is required")

    val repository = createRepository(
        database = createDatabase(url = postgresUrl),
    )

    val fileStorage = createFileStorage(filesDirectory)

    val service = createService(
        repository = repository,
        selector = createSelector(repository),
        clock = Clock.System,
        fileStorage = fileStorage,
    )

    val rpc = createRpc(
        fileGatewayAddress = fileGatewayAddress,
        uploadFilePath = "file/upload",
        downloadFilePath = "file/download",
        authenticator = SillyAuthenticator(),
        service = service,
        assembler = createAssembler(service),
        presenter = createPresenter(service),
    )

    embeddedServer(
        Netty,
        port = port,
        host = host,
    ) {
        install(WebSockets)
        krpcApiModule(rpc)

        routing {
            post("file/upload/{uri}") {
                val uriBase64 = call.parameters["uri"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                val uri = Base64.UrlSafe.decode(uriBase64.replace("_", "="))
                    .toString(Charsets.UTF_8)

                val channel = call.receiveChannel()
                while (!channel.exhausted()) {
                    @OptIn(UnsafeIoApi::class)
                    channel.read { bytes, start, end ->
                        val size = end - start
                        val buffer = Buffer()
                        UnsafeBufferOperations.moveToTail(buffer, bytes, start, end)

                        when (service.file.uploadPart(uri, buffer)) {
                            is UploadFilePartResult.Ok -> {}
                            is UploadFilePartResult.InvalidURI -> {
                                call.respond(HttpStatusCode.NotFound)
                                error("Abort")
                            }
                            is UploadFilePartResult.OwnerStorageQuotaExceeded -> {
                                call.respond(HttpStatusCode.InsufficientStorage)
                                error("Abort")
                            }
                        }

                        size
                    }
                }

                val file = when (
                    val result = service.file.commitParts(uri, types = null)
                ) {
                    is CommitFilePartsResult.Ok -> result.file

                    is CommitFilePartsResult.InvalidFileOwner -> {
                        call.respond(HttpStatusCode.Unauthorized)
                        error("Abort")
                    }

                    is CommitFilePartsResult.InvalidURI -> {
                        call.respond(HttpStatusCode.NotFound)
                        error("Abort")
                    }

                    is CommitFilePartsResult.OwnerStorageQuotaExceeded -> {
                        call.respond(HttpStatusCode.InsufficientStorage)
                        error("Abort")
                    }
                }

                call.respond(HttpStatusCode.OK, """{"fileId":${file.id.long}}""")
            }

            get("file/download/{idOrUri}") {
                val idOrUriBase64 = call.parameters["idOrUri"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val source = if (idOrUriBase64.toLongOrNull() != null) {
                    val id = FileId(idOrUriBase64.toLong())
                    service.file.download(id)
                        ?: return@get call.respond(HttpStatusCode.NotFound)
                } else {
                    val uri = Base64.UrlSafe.decode(idOrUriBase64.replace("_", "="))
                        .toString(Charsets.UTF_8)
                    service.file.download(uri)
                        ?: return@get call.respond(HttpStatusCode.NotFound)
                }

                call.respondBytesWriter(ContentType.Application.OctetStream) {
                    withContext(Dispatchers.IO) {
                        while (!source.exhausted()) {
                            write { bytes, start, end ->
                                source.readAtMostTo(bytes, start, end)
                            }
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}

private fun createRpc(
    fileGatewayAddress: String, //   https://example.com
    uploadFilePath: String,     //   file/upload
    downloadFilePath: String,   //   file/download
    authenticator: Authenticator,
    service: MainService,
    assembler: MainAssembler,
    presenter: MainPresenter,
): MainRpc {
    return MainRpc(
        auth = AuthRpcImpl(authenticator, service, presenter),
        user = UserRpcImpl(authenticator, service, assembler, presenter),
        post = PostRpcImpl(authenticator, service, assembler, presenter),
        file = FileRpcImpl(
            authenticator,
            service,
            assembler,
            presenter,
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
        )
    )
}

private fun createPresenter(service: MainService): MainPresenter {
    var mainPresenter by Delegates.notNull<MainPresenter>()
    mainPresenter = MainPresenter(
        auth = AuthPresenterImpl(service),
        user = UserPresenterImpl({ mainPresenter }, service),
        post = PostPresenterImpl(service),
        file = FilePresenterImpl(),
    )
    return mainPresenter
}

private fun createAssembler(service: MainService): MainAssembler {
    return MainAssembler(
        user = UserAssemblerImpl(service),
        post = PostAssemblerImpl(service),
        file = FileAssemblerImpl(),
    )
}

private fun createService(
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

private fun createSelector(repository: MainRepository): MainSelector {
    return MainSelector(repository)
}

private fun createRepository(database: R2dbcDatabase): MainRepository {
    return MainRepositoryPostgres(
        database = database,
        transactionCoroutineContext = newSingleThreadContext("Repository-Database-Thread"),
    )
}

private fun createFileStorage(directory: String): FileStorage {
    return LocalFileStorage(
        blockingContext = Dispatchers.IO,
        directory = Path(directory),
    )
}

private fun createDatabase(url: String): R2dbcDatabase {
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
