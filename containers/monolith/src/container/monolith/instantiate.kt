@file:Suppress("RedundantSuspendModifier")

package container.monolith

import presentation.infra.jwtManager.JwtManagerDefault
import presentation.infra.jwtManager.PayloadProviderDefault
import presentation.infra.jwtManager.RevokedTokensStoreDefault
import integration.eventCollector.KafkaEventCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import presentation.authenticator.JwtAuthenticator
import presentation.infra.jwtManager.RefreshTokensStoreRedis
import presentation.tokenProvider.JwtTokenProvider
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes


suspend fun instantiate(
    authSecret: String = MonolithDefaults.authSecret,
    redisUrl: String = MonolithDefaults.redisUrl,
    kafkaUrl: String = MonolithDefaults.kafkaUrl,
    postgresUrl: String = MonolithDefaults.postgresUrl,
    fileGatewayAddress: String = MonolithDefaults.fileGatewayAddress,
    filesDirectory: String = MonolithDefaults.filesDirectory,
): Monolith {
    // backend/infrastructure

    val database = createDatabase(url = postgresUrl)
    val redisClient = createRedisClient(url = redisUrl)
    val repository = createRepository(database = database)
    val fileStorage = createFileStorage(filesDirectory)

    // backend/domain

    val service = createService(
        repository = repository,
        eventCollector = KafkaEventCollector(
            scope = GlobalScope + Dispatchers.IO,
            producer = KafkaProducer(
                Properties().apply {
                    put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl)
                    put(ProducerConfig.RETRIES_CONFIG, 3)
                },
                StringSerializer(),
                StringSerializer(),
            )
        ),
        clock = Clock.System,
        fileStorage = fileStorage,
    )

    // presentation/infrastructure

    val jwtManager = JwtManagerDefault(
        payloadProvider = PayloadProviderDefault(
            authService = service.auth,
        ),
        refreshTokensStore = RefreshTokensStoreRedis(
            redisClient = redisClient,
        ),
        revokedTokensStore = RevokedTokensStoreDefault(
            redisClient = redisClient,
        ),
        checkTokenRevoke = false,
        secret = authSecret,
        issuer = "localhost:8080",
        accessTokenLifetime = 1.minutes,
        refreshTokenLifetime = 60.days,
    )

    // Presentation

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

    // Gateway

    val authenticator = JwtAuthenticator(jwtManager = jwtManager)
    val tokenProvider = JwtTokenProvider(jwtManager = jwtManager)

    val rpc = createRpc(
        authenticator = authenticator,
        tokenProvider = tokenProvider,
        controller = controller,
    )

    return Monolith(
        database = database,
        repository = repository,
        fileStorage = fileStorage,
        service = service,
        presenter = presenter,
        assembler = assembler,
        controller = controller,
        jwtManager = jwtManager,
        authenticator = authenticator,
        rpc = rpc,
    )
}
