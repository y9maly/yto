package integration.telegramOpenidConnect

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import kotlin.time.Duration
import kotlin.time.toJavaDuration


@OptIn(ExperimentalLettuceCoroutinesApi::class)
class TelegramPublicKeyCacheRedis(
    private val ttl: Duration,
    private val commands: RedisCoroutinesCommands<String, String>,
) : TelegramPublicKeyCache {
    @Serializable
    class Encoded(val algorithm: String, val key: ByteArray)

    override suspend fun save(publicKey: PublicKey) {
        val encoded = Encoded(publicKey.algorithm, publicKey.encoded)
        val encodedString = Json.encodeToString(encoded)
        commands.set("openidConnectTelegram:publicKeyCache", encodedString, SetArgs()
            .ex(ttl.toJavaDuration()))
    }

    override suspend fun get(): PublicKey? {
        val encodedString = commands.get("openidConnectTelegram:publicKeyCache")
            ?: return null
        val encoded = runCatching { Json.decodeFromString<Encoded>(encodedString) }
            .getOrElse { return null }

        val spec = X509EncodedKeySpec(encoded.key)
        val kf = KeyFactory.getInstance(encoded.algorithm)
        return kf.generatePublic(spec)
    }
}
