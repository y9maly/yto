package y9to.sdk

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import y9to.api.krpc.types.FileSink
import y9to.api.krpc.types.FileSource
import y9to.api.types.File
import y9to.api.types.FileId
import y9to.api.types.FileTypes
import y9to.api.types.UploadFileError
import y9to.libs.io.internals.DelicateIoApi
import y9to.libs.io.segment.ImmutableSegment
import y9to.libs.io.segment.Segment
import y9to.libs.io.segment.snapshot
import y9to.libs.stdlib.Union
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.successOrElse
import y9to.sdk.types.ReadSegmentScope
import y9to.sdk.types.WriteSegmentScope
import kotlinx.io.UnsafeIoApi as UnsafeKotlinxIoApi


actual class FileClient internal actual constructor(
    private val client: Client,
) {
    actual suspend fun get(id: FileId): File? {
        return client.rpc.file.get(client.token, id)
    }

    actual suspend fun download(id: FileId, read: suspend ReadSegmentScope.() -> Unit): Boolean {
        val source = client.rpc.file.download(client.token, id)
            ?: return false

        when (source) {
            is FileSource.HttpOctetStream -> {
                return downloadHttp(source.url, read)
            }
        }
    }

    actual suspend fun upload(
        name: String,
        types: FileTypes?,
        expectedSize: Long?,
        write: suspend WriteSegmentScope.() -> Unit,
    ): Union<File, UploadFileError> {
        val sink = client.rpc.file.upload(
            token = client.token,
            name = name,
            types = types,
            expectedSize = expectedSize,
        ).successOrElse { error ->
            return error.asError()
        }

        when (sink) {
            is FileSink.HttpOctetStream -> {
                return uploadHttp(sink.url, write)
            }
        }
    }

    private suspend fun downloadHttp(
        url: String,
        read: suspend ReadSegmentScope.() -> Unit,
    ): Boolean {
        val httpClient = client.httpClient as HttpClient
        val response = httpClient.get(url)
        if (response.status != HttpStatusCode.OK)
            return false

        val channel = response.bodyAsChannel()
        channel.writeInto(read)

        return true
    }

    private suspend fun uploadHttp(
        url: String,
        write: suspend WriteSegmentScope.() -> Unit,
    ): Union<File, UploadFileError> {
        val httpClient = client.httpClient as HttpClient

        val response = coroutineScope {
            val channel = ByteChannel(autoFlush = true)

            launch {
                write.writeInto(channel)
                channel.flushAndClose()
            }

            httpClient.post(url) {
                timeout {
                    // uploading may take a long of time, it's ok
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                }

                header(HttpHeaders.ContentDisposition, ContentType.Application.OctetStream)
                setBody(channel)
            }
        }

        when (response.status) {
            HttpStatusCode.OK -> {}
            HttpStatusCode.NotFound -> error("Upload session was expired")
            HttpStatusCode.InsufficientStorage -> return UploadFileError.StorageQuotaExceeded.asError()
            else -> return UploadFileError.StorageQuotaExceeded.asError()
        }

        val json = Json.parseToJsonElement(response.bodyAsText())
        val fileId = json.jsonObject
            .getValue("fileId")
            .jsonPrimitive
            .long

        val file = client.rpc.file.get(client.token, FileId(fileId))
            ?: error("Failed to upload file")

        return file.asOk()
    }
}

@OptIn(DelicateIoApi::class, UnsafeKotlinxIoApi::class)
private suspend fun (suspend WriteSegmentScope.() -> Unit).writeInto(
    sink: ByteWriteChannel,
    onSegment: suspend (Segment) -> Unit = {},
): Unit = coroutineScope {
    val read = this@writeInto

    read.invoke(object : WriteSegmentScope {
        override suspend fun write(segment: Segment) {
            val byteArray = segment.snapshot().byteArray

            var written = 0
            var remaining = byteArray.size
            while (remaining > 0) {
                sink.write { memory, start, end ->
                    val size = minOf(remaining, end - start)
                    byteArray.copyInto(memory, destinationOffset = start, startIndex = written, endIndex = written + size)
                    remaining -= size
                    written += size
                    size
                }
            }

            onSegment(segment)
        }
    })
}

@OptIn(DelicateIoApi::class, UnsafeKotlinxIoApi::class)
private suspend fun ByteReadChannel.writeInto(
    sink: suspend ReadSegmentScope.() -> Unit,
): Unit = coroutineScope {
    val channel = this@writeInto

    val segmentsChannel = Channel<Segment>()

    launch {
        while (!channel.exhausted()) {
            channel.read { bytes, start, end ->
                val size = end - start
                segmentsChannel.send(ImmutableSegment(bytes.copyOfRange(start, end), 0, size))
                size
            }
        }

        segmentsChannel.close()
    }

    sink.invoke(object : ReadSegmentScope {
        override suspend fun read(): Segment? {
            return segmentsChannel.receiveCatching().getOrNull()
        }
    })
}
