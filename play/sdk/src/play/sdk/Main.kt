package play.sdk

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import y9to.api.types.FileId
import y9to.libs.io.internals.DelicateIoApi
import y9to.libs.io.segment.ImmutableSegment
import y9to.libs.io.segment.snapshot
import y9to.libs.stdlib.errorOrElse
import y9to.libs.stdlib.successOrElse
import y9to.sdk.Client
import y9to.sdk.createSdkClient
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readBytes


suspend fun main(): Unit = coroutineScope {
    val sdk = createSdkClient("localhost", 8103, "/")

    testUploadDownload(sdk, Path("/Users/mali/IdeaProjects/yto/.testFiles/50m.jpg"))
}

@OptIn(DelicateIoApi::class)
suspend fun testUploadDownload(sdk: Client, filepath: Path) {
    check(Files.exists(filepath)) { "File $filepath does not exist" }
    check(Files.isRegularFile(filepath))
    val newFilepath = filepath.resolveSibling(filepath.nameWithoutExtension + "_downloaded." + filepath.extension)
    check(!Files.exists(newFilepath)) { "File $newFilepath already exist" }

    // StorageQuotaExceeded
    val largeFileResult = sdk.file.upload(name = "largeFile", expectedSize = Long.MAX_VALUE) {}
    println("Upload large file result: $largeFileResult")

    println("Uploading ${Files.size(filepath)} bytes...")
    val file = sdk.file.upload(name = "file") {
        val byteArray = filepath.readBytes()

        var written = 0
        var remaining = byteArray.size
        while (remaining > 0) {
            delay(100)
            val size = minOf(remaining, 1024 * 1024)
            write(ImmutableSegment(byteArray.copyOfRange(written, written + size)))

            written += size
            remaining -= size
        }
    }.successOrElse { error(it) }

    println("File uploaded: $file")
    println("Downloading...")
    val buffer = Buffer()
    val downloadSuccess = sdk.file.download(FileId(115)) {
        while (true) {
            val segment = read() ?: break
            buffer.write(segment.snapshot().byteArray)
        }
    }
    check(downloadSuccess) { "Download failed" }
    println("Downloaded ${buffer.size} bytes")
    Files.write(newFilepath, buffer.readByteArray())
}
