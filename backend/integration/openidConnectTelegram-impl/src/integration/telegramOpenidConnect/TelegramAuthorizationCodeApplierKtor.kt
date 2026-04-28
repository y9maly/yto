package integration.telegramOpenidConnect

import backend.core.types.SessionId
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64


class TelegramAuthorizationCodeApplierKtor(
    private val clientId: String,
    clientSecret: String,
    // "https://oauth.telegram.org/token"
    private val telegramTokenUrl: suspend () -> String,
    private val httpClient: suspend () -> HttpClient
): TelegramAuthorizationCodeApplier {
    private val authorizationHeader = "Basic " + Base64.encode("$clientId:$clientSecret".toByteArray())

    override suspend fun check(
        redirectUri: String,
        authorizationCode: String,
        codeVerifier: String
    ): String {
        val telegramTokenUrl = telegramTokenUrl()

        val response = httpClient().post(telegramTokenUrl) {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            header(HttpHeaders.Authorization, authorizationHeader)
            setBody(FormDataContent(Parameters.build {
                append("grant_type", "authorization_code")
                append("code", authorizationCode)
                append("redirect_uri", redirectUri)
                append("client_id", clientId)
                append("code_verifier", codeVerifier)
            }))
        }.bodyAsText()

        runCatching {
            Json.parseToJsonElement(response)
        }.onFailure { failure ->
            throw RuntimeException("Failed to parse telegram authorization response body: '$response'", failure)
        }

        return response
    }
}
