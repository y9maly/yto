package integration.telegramOpenidConnect


sealed interface ValidateTelegramOpenidConnectResult {
    data class Ok(val profile: TelegramOIDCProfile) : ValidateTelegramOpenidConnectResult
    data object InvalidAuthorizationCode : ValidateTelegramOpenidConnectResult
}
