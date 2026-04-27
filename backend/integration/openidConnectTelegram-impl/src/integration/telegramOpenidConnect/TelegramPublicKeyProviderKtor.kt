package integration.telegramOpenidConnect

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.security.PublicKey


class TelegramPublicKeyProviderKtor(
    private val cache: TelegramPublicKeyCache,
    // "https://oauth.telegram.org/.well-known/jwks.json"
    private val wellKnownJWKSUrl: () -> String,
    private val httpClient: suspend () -> HttpClient
) : TelegramPublicKeyProvider {
    override suspend fun provide(): PublicKey {
        val cached = cache.get()
        if (cached != null)
            return cached

        val response = httpClient().get(wellKnownJWKSUrl())
            .bodyAsText()
        val jwkSet = JWKSet.parse(response)
        val rsaKey: RSAKey = jwkSet.getKeyByKeyId("oidc-1") as RSAKey
        val publicKey = rsaKey.toRSAPublicKey()
        cache.save(publicKey)
        return publicKey
    }
}
