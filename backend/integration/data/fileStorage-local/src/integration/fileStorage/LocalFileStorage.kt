package integration.fileStorage

import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import org.jetbrains.annotations.Blocking
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.notExists
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
class LocalFileStorage(
    private val blockingContext: CoroutineContext,
    private val directory: Path,
) : FileStorage {
    private fun path(uri: String) = directory.resolve(uri)

    @Blocking
    private fun create(): String {
        val uri = Uuid.generateV7().toHexString()
        Files.createFile(path(uri))
        return uri
    }

    override suspend fun create(bytes: Source?): CreateResult {
        return createAndClose(bytes)
    }

    override suspend fun createAndClose(bytes: Source?): CreateResult = withContext(blockingContext) {
        val uri = create()
        if (bytes != null) {
            Files.newOutputStream(path(uri)).use { stream ->
                bytes.transferTo(stream.asSink())
            }
        }
        CreateResult.Ok(uri, sizeBytes = Files.size(path(uri)))
    }

    override suspend fun append(uri: String, bytes: Source): AppendResult {
        return appendAndClose(uri, bytes)
    }

    override suspend fun appendAndClose(
        uri: String,
        bytes: Source
    ): AppendResult = withContext(blockingContext) {
        val path = path(uri)
        if (path.notExists())
            return@withContext AppendResult.InvalidURI
        Files.newOutputStream(path, StandardOpenOption.APPEND).use { stream ->
            bytes.transferTo(stream.asSink())
        }
        AppendResult.Ok(newSizeBytes = Files.size(path))
    }

    override suspend fun close(uri: String): CloseResult {
        if (!path(uri).exists())
            return CloseResult.InvalidURI
        return CloseResult.Ok(path(uri).fileSize())
    }

    override suspend fun readFile(uri: String): Source? = withContext(blockingContext) {
        val path = path(uri)
        if (path.notExists())
            return@withContext null
        val stream = Files.newInputStream(path)
        stream.asSource().buffered()
    }

    override suspend fun delete(uri: String): Boolean {
        return path(uri).deleteIfExists()
    }
}
