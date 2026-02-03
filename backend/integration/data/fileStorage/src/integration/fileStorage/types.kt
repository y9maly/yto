package integration.fileStorage


sealed interface CreateResult {
    data class Ok(val uri: String, val sizeBytes: Long) : CreateResult
}

sealed interface AppendResult {
    data class Ok(val newSizeBytes: Long) : AppendResult
    data object InvalidURI : AppendResult
}

sealed interface CloseResult {
    data class Ok(val totalSizeBytes: Long) : CloseResult
    data object InvalidURI : CloseResult
}
