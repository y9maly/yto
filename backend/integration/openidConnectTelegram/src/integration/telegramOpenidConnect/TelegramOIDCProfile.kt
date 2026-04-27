package integration.telegramOpenidConnect

import y9to.libs.stdlib.optional.Optional


data class TelegramOIDCProfile(
    /**
     * "subject" (or "sub"). see https://core.telegram.org/bots/telegram-login#user-data-structure
     */
    val authorizationId: String,
    val id: Optional<Long>,
    val name: Optional<String>,
    val preferredUsername: Optional<String>,
    val avatar: Optional<String>,
    val phoneNumber: Optional<String>,
)
