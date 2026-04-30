package y9to.sdk.services

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.Source
import y9to.libs.stdlib.ifError
import y9to.libs.stdlib.ifSuccess
import y9to.sdk.Client
import y9to.sdk.types.UploadFileState
import y9to.sdk.upload
import kotlin.coroutines.cancellation.CancellationException


interface UploadFileService {
    fun upload(filename: String, filesize: Long?, source: Source): Flow<UploadFileState>
}

class UploadFileServiceDefault(private val client: Client) : UploadFileService {
    val logger = client.logger("UploadFileService")

    override fun upload(
        filename: String,
        filesize: Long?,
        source: Source
    ): Flow<UploadFileState> = flow {
        emit(UploadFileState.Connecting(0L, filesize))

        try {
            val result = client.file.upload(
                name = filename,
                expectedSize = filesize,
                source = source,
            )

            // todo emit uploading progress

            result.ifSuccess {
                emit(UploadFileState.Done(it))
            }.ifError {
                emit(UploadFileState.Error(it))
            }
        } catch (e: CancellationException) {
            emit(UploadFileState.Cancelled(0L, filesize))
            throw e
        } catch (t: Throwable) {
            logger.info(t) { "Failed to upload file '$filename'" }
            emit(UploadFileState.Exception(t))
        }
    }
}
