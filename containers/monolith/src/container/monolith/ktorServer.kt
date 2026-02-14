package container.monolith

import backend.core.types.FileId
import domain.service.result.CommitFilePartsResult
import domain.service.result.UploadFilePartResult
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
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
import kotlinx.io.Buffer
import kotlinx.io.UnsafeIoApi
import kotlinx.io.unsafe.UnsafeBufferOperations
import presentation.gateway.ktorKrpc.krpcApiModule
import kotlin.io.encoding.Base64


fun Monolith.startKtorServer(host: String, port: Int, wait: Boolean) {
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
    }.start(wait = wait)
}
