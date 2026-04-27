package integration.telegramOpenidConnect


interface TelegramOpenidConnect {
    suspend fun initiate(
        redirectUri: String,
        requestProfile: Boolean,
        requestPhoneNumber: Boolean,
        requestBotAccess: Boolean,
    ): InitiateTelegramOpenidConnectResult

    suspend fun validate(
        redirectUri: String,
        authorizationCode: String,
        codeVerifier: String,
    ): ValidateTelegramOpenidConnectResult
}
