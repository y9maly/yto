package integration.telegramOpenidConnect

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import y9to.libs.stdlib.optional.presentIfNotNull
import java.security.MessageDigest
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
class TelegramOpenidConnectImpl(
    private val publicKeyProvider: TelegramPublicKeyProvider,
    private val authorizationCodeApplier: TelegramAuthorizationCodeApplier,
    private val clientId: String,
    private val botId: String,
    // "https://oauth.telegram.org"
    private val telegramIssuer: String,
) : TelegramOpenidConnect {
    companion object {
        internal val logger = KotlinLogging.logger { }
    }

    override suspend fun initiate(
        redirectUri: String,
        requestProfile: Boolean,
        requestPhoneNumber: Boolean,
        requestBotAccess: Boolean
    ): InitiateTelegramOpenidConnectResult {
        val state = generateState()
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = createCodeChallenge(codeVerifier)

        val authorizationUri = buildString {
            append("https://oauth.telegram.org/auth?")
            append("client_id=$clientId&")
            append("redirect_uri=$redirectUri&")
            append("response_type=code&")

            append("scope=openid")
            if (requestProfile)
                append("%20profile")
            if (requestPhoneNumber)
                append("%20phone")
            if (requestBotAccess)
                append("%20telegram:bot_access")
            append("&")

            append("state=$state&")
            append("code_challenge=$codeChallenge&")
            append("code_challenge_method=S256")
        }

        return InitiateTelegramOpenidConnectResult(
            codeVerifier = codeVerifier,
            authorizationState = state,
            authorizationUri = authorizationUri,
        )
    }

    override suspend fun validate(
        redirectUri: String,
        authorizationCode: String,
        codeVerifier: String
    ): ValidateTelegramOpenidConnectResult {
        val telegramPublicKey: PublicKey = publicKeyProvider.provide()

        val responseString = authorizationCodeApplier.check(
            redirectUri = redirectUri,
            authorizationCode = authorizationCode,
            codeVerifier = codeVerifier,
        )

        val response = runCatching {
            Json.decodeFromString<CodeCheckResponse>(responseString)
        }.getOrElse {
            return ValidateTelegramOpenidConnectResult.InvalidAuthorizationCode
        }

        val telegramOAuthProfile = runCatching {
            createTelegramOAuthProfile(response.idToken, telegramPublicKey)
        }.getOrElse { failure ->
            logger.warn(failure) { "Cannot create TelegramOAuthProfile from idToken from telegram response: '$responseString'" }
            return ValidateTelegramOpenidConnectResult.InvalidAuthorizationCode
        }

        return ValidateTelegramOpenidConnectResult.Ok(
            profile = telegramOAuthProfile
        )
    }

    private fun createTelegramOAuthProfile(idToken: String, publicKey: PublicKey): TelegramOAuthProfile {
        val rsaPublicKey = when (publicKey.algorithm.uppercase()) {
            "RSA" -> publicKey as RSAPublicKey
            else -> {
                val message = "Unsupported publicKey algorithm '${publicKey.algorithm}'. publicKey=${publicKey.encoded}"
                logger.error { message }
                error(message)
            }
        }

        val jwt = try {
            JWT.require(Algorithm.RSA256(rsaPublicKey, null))
                .withIssuer(telegramIssuer)
                .withAudience(botId)
                .build()
                .verify(idToken)
        } catch (e: AlgorithmMismatchException) {
            throw e
        } catch (e: SignatureVerificationException) {
            throw e
        } catch (e: JWTVerificationException) {
            throw e
        } catch (e: TokenExpiredException) {
            throw e
        } catch (e: MissingClaimException) {
            throw e
        } catch (e: IncorrectClaimException) {
            throw e
        }

        return TelegramOAuthProfile(
            authorizationId = jwt.subject,
            id = presentIfNotNull(jwt.getClaim("id").asLong()),
            name = presentIfNotNull(jwt.getClaim("name").asString()),
            preferredUsername = presentIfNotNull(jwt.getClaim("preferred_username").asString()),
            avatar = presentIfNotNull(jwt.getClaim("picture").asString()),
            phoneNumber = presentIfNotNull(jwt.getClaim("phone_number").asString()),
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
private class CodeCheckResponse(
    @JsonNames("access_token")
    val accessToken: String,
    @JsonNames("id_token")
    val idToken: String,
    @JsonNames("token_type")
    val tokenType: String,
    @JsonNames("expires_in")
    val expiresIn: Int,
    @JsonNames("scope")
    val scope: String = "openid",
)

private fun generateState(): String =
    generateRandomString(64)

private fun generateCodeVerifier(): String =
    generateRandomString(64)

private fun createCodeChallenge(codeVerifier: String): String {
    val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
    val md = MessageDigest.getInstance("SHA-256")
    md.update(bytes)
    val digest = md.digest()
    val result = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(digest)
    return result
}

private fun generateRandomString(length: Int): String {
    require(length >= 0) { "length cannot be negative" }
    return (1..length).map { "qwertyuiopasdfghjklzxcvbnm1234567890".random() }.joinToString("")
}
