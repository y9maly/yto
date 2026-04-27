package integration.telegramOpenidConnect

import java.security.PublicKey


interface TelegramPublicKeyProvider {
    suspend fun provide(): PublicKey
}
