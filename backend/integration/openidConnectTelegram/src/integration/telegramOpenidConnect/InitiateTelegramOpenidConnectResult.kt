package integration.telegramOpenidConnect


data class InitiateTelegramOpenidConnectResult(
    val authorizationUri: String,
    val authorizationState: String,
    val codeVerifier: String,
)
