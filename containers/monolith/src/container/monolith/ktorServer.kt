package container.monolith

import backend.core.types.FileId
import backend.core.types.SessionId
import domain.service.CheckOAuthError
import domain.service.ContinueLoginError
import domain.service.result.CommitFilePartsResult
import domain.service.result.UploadFilePartResult
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.read
import io.ktor.utils.io.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.span
import kotlinx.html.title
import kotlinx.io.Buffer
import kotlinx.io.UnsafeIoApi
import kotlinx.io.unsafe.UnsafeBufferOperations
import presentation.gateway.ktorKrpc.krpcApiModule
import y9to.libs.stdlib.successOrElse
import java.io.File
import kotlin.io.encoding.Base64


private val ktorServerLogger = KotlinLogging.logger { }

data class StartKtorServerConfig(
    val host: String,
    val port: Int,

    // null = no cors plugin
    val cors: Cors?,

    val staticFiles: List<StaticFiles> = emptyList(),
) {
    data class Cors(
        // null = any host
        // format: <schema>://<optional subdomain>.<domain>.<zone>
        // for example: listOf("http://domain.zone", "https://domain.zone", "https://subdomain.domain.zone")
        val hosts: List<String>?,
    )

    data class StaticFiles(
        val remotePath: String,
        val directory: String,
        val default: String? = null,
    )
}

fun Monolith.startKtorServer(
    config: StartKtorServerConfig,
    wait: Boolean,
) {
    embeddedServer(
        Netty,
        port = config.port,
        host = config.host,
    ) {
        install(ContentNegotiation)

        install(WebSockets)

        krpcApiModule(rpc)

        if (config.cors != null) {
            ktorServerLogger.debug { "Ktor server configured with CORS: ${config.cors}" }

            install(CORS) {
                allowNonSimpleContentTypes = true

                allowHeaders { true }
                allowHeader(HttpHeaders.ContentType)
                allowHeader(HttpHeaders.ContentDisposition)
                allowHeader(HttpHeaders.Authorization)
                allowHeader(HttpHeaders.Accept)

                anyMethod()

                if (config.cors.hosts == null) {
                    ktorServerLogger.debug { "Ktor server configured with anyHost" }
                    anyHost()
                } else {
                    config.cors.hosts.forEach { urlString ->
                        if ("://" !in urlString)
                            error("Scheme in '$urlString' is required (http/https for example)")
                        val schema = urlString.substringBefore("://")
                        val host = urlString.substringAfter("://")
                        ktorServerLogger.debug { "Ktor server configured with allowHost(host=$host, schema=$schema)" }
                        allowHost(host, schemes = listOf(schema))
                    }
                }
            }
        } else {
            ktorServerLogger.debug { "Ktor server configured with no CORS" }
        }

        routing {
            config.staticFiles.forEach { staticFiles ->
                ktorServerLogger.debug { "Ktor server configured with staticFiles(remotePath=${staticFiles.remotePath}, directory=${staticFiles.directory}, default=${staticFiles.default})" }

                staticFiles(
                    remotePath = staticFiles.remotePath,
                    dir = File(staticFiles.directory),
                ) {
                    default(staticFiles.default)
                }
            }

            get("/login/telegramOAuth/{sessionId}") {
                val sessionId = call.parameters["sessionId"]!!.toLong()

                loginService.checkOAuth(
                    session = SessionId(sessionId),
                    authorizationCode = call.parameters["code"]!!,
                    authorizationState = call.parameters["state"]!!,
                ).successOrElse { error ->
                    when (error) {
                        CheckOAuthError.InvalidAuthorizationCode -> call.respondHtml(BadRequest) {
                            head { title { +"Login with Telegram Error" } }

                            body {
                                span {
                                    +"Invalid link. Provided code is invalid"
                                }
                            }
                        }

                        CheckOAuthError.InvalidAuthorizationState -> call.respondHtml(BadRequest) {
                            head { title { +"Login with Telegram Error" } }

                            body {
                                span {
                                    +"Invalid link. Provided state is invalid"
                                }
                            }
                        }

                        ContinueLoginError.InvalidSessionId -> call.respondHtml(Unauthorized) {
                            head { title { +"Login with Telegram Error" } }

                            body {
                                span {
                                    +"Unauthorized"
                                }
                            }
                        }

                        ContinueLoginError.LoginAttemptRejected -> call.respondHtml(Forbidden) {
                            head { title { +"Login with Telegram Error" } }

                            body {
                                span {
                                    +"Login attempt rejected"
                                }
                            }
                        }

                        ContinueLoginError.Unexpected -> call.respondHtml(NotFound) {
                            head { title { +"Login with Telegram Error" } }

                            body {
                                span {
                                    +"Unexpected"
                                }
                            }
                        }
                    }

                    return@get
                }

                call.respondHtml(OK) {
                    head { title { +"Login with Telegram Success" } }

                    body {
                        span {
                            +"Successfully logged in. Please return to the application"
                        }
                    }
                }
            }

            post("file/upload/{uri}") {
                val uriBase64 = call.parameters["uri"]
                    ?: return@post call.respond(BadRequest)
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
                                call.respond(NotFound)
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
                        call.respond(Unauthorized)
                        error("Abort")
                    }

                    is CommitFilePartsResult.InvalidURI -> {
                        call.respond(NotFound)
                        error("Abort")
                    }

                    is CommitFilePartsResult.OwnerStorageQuotaExceeded -> {
                        call.respond(HttpStatusCode.InsufficientStorage)
                        error("Abort")
                    }
                }

                call.respond(OK, """{"fileId":${file.id.long}}""")
            }

            get("file/download/{idOrUri}") {
                val idOrUriBase64 = call.parameters["idOrUri"]
                    ?: return@get call.respond(BadRequest)

                val source = if (idOrUriBase64.toLongOrNull() != null) {
                    val id = FileId(idOrUriBase64.toLong())
                    service.file.download(id)
                        ?: return@get call.respond(NotFound)
                } else {
                    val uri = Base64.UrlSafe.decode(idOrUriBase64.replace("_", "="))
                        .toString(Charsets.UTF_8)
                    service.file.download(uri)
                        ?: return@get call.respond(NotFound)
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
    }.start(wait = wait)
}
