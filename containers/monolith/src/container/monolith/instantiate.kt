@file:Suppress("RedundantSuspendModifier")

package container.monolith

import domain.service.LoginServiceImpl
import integration.eventCollector.KafkaEventCollector
import integration.loginRepository.LoginRepositoryRedis
import integration.telegramOpenidConnect.TelegramAuthorizationCodeApplierKtor
import integration.telegramOpenidConnect.TelegramOpenidConnectImpl
import integration.telegramOpenidConnect.TelegramPublicKeyCacheRedis
import integration.telegramOpenidConnect.TelegramPublicKeyProviderKtor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import presentation.authenticator.JwtAuthenticator
import presentation.infra.jwtManager.JwtManagerDefault
import presentation.infra.jwtManager.PayloadProviderDefault
import presentation.infra.jwtManager.RefreshTokensStoreRedis
import presentation.infra.jwtManager.RevokedTokensStoreDefault
import presentation.infra.updateManager.UpdateManagerDefault
import presentation.infra.updateManager.UpdateProducerRedisLettuce
import presentation.infra.updateManager.UpdateProviderRedisLettuce
import presentation.tokenProvider.JwtTokenProvider
import presentation.updateProvider.UpdateProviderDefault
import presentation.updateSubscriptionsStore.UpdateSubscriptionsStoreRedis
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes


@OptIn(ExperimentalLettuceCoroutinesApi::class)
suspend fun instantiate(
    authSecret: String = MonolithDefaults.authSecret,
    redisUrl: String = MonolithDefaults.redisUrl,
    kafkaUrl: String = MonolithDefaults.kafkaUrl,
    postgresUrl: String = MonolithDefaults.postgresUrl,
    fileGatewayUrl: String = MonolithDefaults.fileGatewayUrl,
    telegramOAuthRedirectUrl: String = MonolithDefaults.telegramOAuthRedirectUrl,
    telegramOAuthClientId: String? = MonolithDefaults.telegramOAuthClientId,
    telegramOAuthClientSecret: String? = MonolithDefaults.telegramOAuthClientSecret,
    filesDirectory: String = MonolithDefaults.filesDirectory,
): Monolith {
    // backend/infrastructure

    val database = createDatabase(url = postgresUrl)
    val redisClient = createRedisClient(url = redisUrl)
    val repository = createRepository(database = database)
    val fileStorage = createFileStorage(filesDirectory)

    val httpClient = HttpClient(CIO)

    // backend/domain

    val eventCollector = KafkaEventCollector(
        scope = GlobalScope + Dispatchers.IO,
        producer = KafkaProducer(
            Properties().apply {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl)
                put(ProducerConfig.RETRIES_CONFIG, 3)
            },
            StringSerializer(),
            StringSerializer(),
        )
    )

    val service = createService(
        repository = repository,
        eventCollector = eventCollector,
        clock = Clock.System,
        fileStorage = fileStorage,
    )

    val loginService = LoginServiceImpl(
        authService = service.auth,
        userService = service.user,
        eventCollector = eventCollector,
        telegramOpenidConnect = if (telegramOAuthClientId != null && telegramOAuthClientSecret != null) {
            TelegramOpenidConnectImpl(
                publicKeyProvider = TelegramPublicKeyProviderKtor(
                    cache = TelegramPublicKeyCacheRedis(
                        ttl = 5.minutes,
                        commands = redisClient.connect().coroutines()
                    ),
                    wellKnownJWKSUrl = { "https://oauth.telegram.org/.well-known/jwks.json" },
                    httpClient = { httpClient },
                ),
                authorizationCodeApplier = TelegramAuthorizationCodeApplierKtor(
                    clientId = telegramOAuthClientId,
                    clientSecret = telegramOAuthClientSecret,
                    telegramTokenUrl = { "https://oauth.telegram.org/token" },
                    httpClient = { httpClient },
                ),
                clientId = telegramOAuthClientId,
                botId = telegramOAuthClientId,
                telegramIssuer = "https://oauth.telegram.org",
            )
        } else null,
        loginRepository = LoginRepositoryRedis(redisClient.connect().coroutines()),
        loginStepTTL = 5.minutes,
        confirmCodeLength = { listOf(4, 6).random() },
        redirectUri = { sessionId ->
            "$telegramOAuthRedirectUrl/login/telegramOAuth/${sessionId.long}"
        },
        requiredToLinkPhoneNumberWhileTelegramOAuthRegistration = false,
        debugLoginCodes = setOf("123"),
    )

    // presentation/infrastructure

    val updateProducer = UpdateProducerRedisLettuce(redisClient.connect().coroutines())
    val updateProvider = UpdateProviderRedisLettuce(redisClient.connect().coroutines())
    val updateManager = UpdateManagerDefault(
        updateProducer = updateProducer,
        updateProvider = updateProvider,
    )

    val updateSubscriptionsStore = UpdateSubscriptionsStoreRedis(redisClient.connect().coroutines())

    val jwtManager = JwtManagerDefault(
        payloadProvider = PayloadProviderDefault(
            authService = service.auth,
        ),
        refreshTokensStore = RefreshTokensStoreRedis(
            commands = redisClient.connect().coroutines(),
        ),
        revokedTokensStore = RevokedTokensStoreDefault(
            commands = redisClient.connect().coroutines(),
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
        fileGatewayUrl = fileGatewayUrl,
        uploadFilePath = "file/upload",
        downloadFilePath = "file/download",
        service = service,
        loginService = loginService,
        assembler = assembler,
        presenter = presenter,
        updateProvider = UpdateProviderDefault(updateManager),
        updateSubscriptionsStore = updateSubscriptionsStore,
    )

    // Gateway

    val authenticator = JwtAuthenticator(jwtManager = jwtManager)
    val tokenProvider = JwtTokenProvider(jwtManager = jwtManager)

    val rpc = createRpc(
        authenticator = authenticator,
        tokenProvider = tokenProvider,
        controller = controller,
        authService = service.auth,
    )

    return Monolith(
        database = database,
        repository = repository,
        fileStorage = fileStorage,
        service = service,
        loginService = loginService,
        presenter = presenter,
        assembler = assembler,
        controller = controller,
        updateManager = updateManager,
        updateSubscriptionsStore = updateSubscriptionsStore,
        jwtManager = jwtManager,
        authenticator = authenticator,
        rpc = rpc,
    )
}
