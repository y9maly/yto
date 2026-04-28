package integration.telegramOpenidConnect


sealed interface ValidateTelegramOpenidConnectResult {
    data class Ok(val profile: TelegramOAuthProfile) : ValidateTelegramOpenidConnectResult
    data object InvalidAuthorizationCode : ValidateTelegramOpenidConnectResult
}
