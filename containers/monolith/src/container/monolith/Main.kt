package container.monolith

import domain.selector.MainSelector
import domain.service.AuthServiceImpl
import domain.service.MainService
import domain.service.PostServiceImpl
import domain.service.UserServiceImpl
import integration.repository.MainRepository
import integration.repository.MainRepositoryPostgres
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.newSingleThreadContext
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import presentation.api.krpc.AuthRpcImpl
import presentation.api.krpc.PostRpcImpl
import presentation.api.krpc.UserRpcImpl
import presentation.assembler.MainAssembler
import presentation.assembler.PostAssemblerImpl
import presentation.assembler.UserAssemblerImpl
import presentation.authenticator.Authenticator
import presentation.authenticator.SillyAuthenticator
import presentation.gateway.ktorKrpc.krpcApiModule
import presentation.presenter.AuthPresenterImpl
import presentation.presenter.MainPresenter
import presentation.presenter.PostPresenterImpl
import presentation.presenter.UserPresenterImpl
import y9to.api.krpc.MainRpc
import java.io.File
import kotlin.time.Clock


fun main() {
    val repository = createRepository(
        database = createDatabase(),
    )

    val service = createService(
        repository = repository,
        selector = createSelector(repository),
        clock = Clock.System,
    )

    val rpc = createRpc(
        authenticator = SillyAuthenticator(),
        service = service,
        assembler = createAssembler(service),
        presenter = createPresenter(service),
    )

    embeddedServer(
        Netty,
        port = 8103,
        host = "0.0.0.0",
    ) {
        install(WebSockets)
        krpcApiModule(rpc)
    }.start(wait = true)
}

private fun createRpc(
    authenticator: Authenticator,
    service: MainService,
    assembler: MainAssembler,
    presenter: MainPresenter,
): MainRpc {
    return MainRpc(
        auth = AuthRpcImpl(authenticator, service, presenter),
        user = UserRpcImpl(authenticator, service, assembler, presenter),
        post = PostRpcImpl(authenticator, service, assembler, presenter),
    )
}

private fun createPresenter(service: MainService): MainPresenter {
    return MainPresenter(
        auth = AuthPresenterImpl(service),
        user = UserPresenterImpl(service),
        post = PostPresenterImpl(service),
    )
}

private fun createAssembler(service: MainService): MainAssembler {
    return MainAssembler(
        user = UserAssemblerImpl(service),
        post = PostAssemblerImpl(service),
    )
}

private fun createService(
    repository: MainRepository,
    selector: MainSelector,
    clock: Clock,
): MainService {
    return MainService(
        auth = AuthServiceImpl(repository, clock),
        user = UserServiceImpl(repository, selector, clock),
        post = PostServiceImpl(repository, selector, clock),
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

private fun createDatabase(): R2dbcDatabase {
    return R2dbcDatabase.connect(
        url = "r2dbc:postgresql://postgres:${File("/Users/mali/penis").readText()}@localhost:5050/db",
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
