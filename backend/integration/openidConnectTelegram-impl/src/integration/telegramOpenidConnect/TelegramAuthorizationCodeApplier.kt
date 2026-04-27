package integration.telegramOpenidConnect


interface TelegramAuthorizationCodeApplier {
    /**
     * @return JSON string. https://core.telegram.org/bots/telegram-login#exchange-code-for-tokens
     */
    suspend fun check(
        redirectUri: String,
        authorizationCode: String,
        codeVerifier: String,
    ): String
}
