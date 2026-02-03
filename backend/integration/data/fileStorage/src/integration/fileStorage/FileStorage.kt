package integration.fileStorage

import kotlinx.io.Source


interface FileStorage {
    /**
     * Create a new file and write [bytes] to it and do not close the resource now for optimization reasons
     * (implementation-defined, for example, file descriptor for LocalFileStorage)
     * (implementation may close resource if needed, for example, if low memory)
     * because [append] will be called soon
     * @param bytes if null creates zero-length file
     */
    suspend fun create(bytes: Source?): CreateResult

    /**
     * Create a new file and write [bytes] to it and close the resource.
     * @param bytes if null creates zero-length file
     */
    suspend fun createAndClose(bytes: Source?): CreateResult

    /**
     * Append bytes to an existing file and do not close the resource now for optimization reasons
     * (implementation-defined, for example, file descriptor for LocalFileStorage)
     * (implementation may close the resource if needed, for example, if low memory)
     * because [append] will be called soon
     */
    suspend fun append(uri: String, bytes: Source): AppendResult

    /**
     * Append bytes to an existing file and close the resource
     */
    suspend fun appendAndClose(uri: String, bytes: Source): AppendResult

    /**
     * Close the resource because there are no bytes to [append] now
     */
    suspend fun close(uri: String): CloseResult

    suspend fun readFile(uri: String): Source?

    /**
     * @return true if the existing file was successfully deleted, false if the file does not exist.
     */
    suspend fun delete(uri: String): Boolean
}
