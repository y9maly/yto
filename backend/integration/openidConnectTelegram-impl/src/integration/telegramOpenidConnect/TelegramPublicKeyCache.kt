package integration.telegramOpenidConnect

import java.security.PublicKey


interface TelegramPublicKeyCache {
    suspend fun save(publicKey: PublicKey)
    suspend fun get(): PublicKey?
}
