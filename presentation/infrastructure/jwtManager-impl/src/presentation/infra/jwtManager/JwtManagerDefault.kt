package presentation.infra.jwtManager

import backend.core.types.AuthState
import backend.core.types.ClientId
import backend.core.types.SessionId
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toKotlinInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
class JwtManagerDefault(
    private val payloadProvider: PayloadProvider,
    private val refreshTokensStore: RefreshTokensStore,
    private val revokedTokensStore: RevokedTokensStore,
    private val checkTokenRevoke: Boolean,
    private val secret: String,
    private val issuer: String,
    private val accessTokenLifetime: Duration,
    private val refreshTokenLifetime: Duration,
) : JwtManager {
    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier = JWT
        .require(algorithm)
        .build()

    override suspend fun authenticate(accessToken: String): AuthenticateResult {
        try {
            val decodedJwt = verifier.verify(accessToken)

            val jti = decodedJwt.getClaim("jti")?.asString()
                ?: return AuthenticateResult.InvalidAccessToken

            if (checkTokenRevoke && revokedTokensStore.isRevoked(jti))
                return AuthenticateResult.RevokedAccessToken

            val session = decodedJwt.getClaim("session")?.asLong()?.let(::SessionId)
                ?: return AuthenticateResult.InvalidAccessToken

            val client = Json.decodeFromString<ClientId?>(
                decodedJwt.getClaim("client")?.asString() ?: return AuthenticateResult.InvalidAccessToken
            )

            return AuthenticateResult.Ok(AccessTokenPayload(
                sessionId = session,
                authState =
                    if (client == null) AuthState.Unauthorized
                    else AuthState.Authorized(client)
            ))
        } catch (_: TokenExpiredException) {
            return AuthenticateResult.ExpiredAccessToken
        } catch (_: SerializationException) {
            return AuthenticateResult.InvalidAccessToken
        } catch (_: AlgorithmMismatchException) {
            return AuthenticateResult.InvalidAccessToken
        } catch (_: JWTVerificationException) {
            return AuthenticateResult.InvalidAccessToken
        }
    }

    override suspend fun issueTokens(forSession: SessionId): IssueTokensResult {
        val accessToken = generateAccessToken(
            payloadProvider.getAccessTokenPayload(forSession)
                ?: return IssueTokensResult.InvalidSessionId
        )

        val refreshToken = generateRefreshToken(RefreshTokenPayload(
            sessionId = forSession,
        ))

        refreshTokensStore.updateRefreshTokenJti(forSession, refreshToken.jti)

        return IssueTokensResult.Ok(accessToken = accessToken, refreshToken = refreshToken.string)
    }

    override suspend fun refreshTokens(refreshToken: String): RefreshTokensResult {
        try {
            val decodedJwt = verifier.verify(refreshToken)

            val jti = decodedJwt.getClaim("jti")?.asString()
                ?: return RefreshTokensResult.InvalidRefreshToken

            val session = decodedJwt.getClaim("session")?.asLong()?.let(::SessionId)
                ?: return RefreshTokensResult.InvalidRefreshToken

            val expectedJti = refreshTokensStore.getRefreshTokenJti(session)

            if (jti != expectedJti)
                return RefreshTokensResult.ExpiredRefreshToken

            val newAccessToken = generateAccessToken(
                payloadProvider.getAccessTokenPayload(session)
                    ?: return RefreshTokensResult.ExpiredRefreshToken
            )

            val newRefreshToken = generateRefreshToken(RefreshTokenPayload(
                sessionId = session,
            ))

            refreshTokensStore.updateRefreshTokenJti(session, newRefreshToken.jti)

            return RefreshTokensResult.Ok(accessToken = newAccessToken, refreshToken = newRefreshToken.string)
        } catch (_: TokenExpiredException) {
            return RefreshTokensResult.ExpiredRefreshToken
        } catch (_: AlgorithmMismatchException) {
            return RefreshTokensResult.InvalidRefreshToken
        } catch (_: JWTVerificationException) {
            return RefreshTokensResult.InvalidRefreshToken
        }
    }

    override suspend fun revokeAccessToken(accessToken: String) {
        try {
            val decodedJwt = verifier.verify(accessToken)

            val expiresAt = decodedJwt.expiresAtAsInstant!!.toKotlinInstant()

            val jti = decodedJwt.getClaim("jti")?.asString()
                ?: return

            revokedTokensStore.revoke(jti, expiresAt)
        } catch (_: Exception) {
            return
        }
    }

    override suspend fun revokeRefreshToken(forSession: SessionId) {
        refreshTokensStore.deleteRefreshTokenJti(forSession)
    }

    private data class RefreshToken(val jti: String, val string: String)

    private fun generateRefreshToken(payload: RefreshTokenPayload): RefreshToken {
        val expirationTime = System.currentTimeMillis() + refreshTokenLifetime.inWholeMilliseconds
        val jti = Uuid.generateV7().toString()
        val string = JWT.create()
            .withIssuer(issuer)
            .withClaim("session", payload.sessionId.long)
            .withExpiresAt(Instant.ofEpochMilli(expirationTime))
            .withJWTId(jti)
            .sign(algorithm)
        return RefreshToken(jti = jti, string = string)
    }

    private fun generateAccessToken(payload: AccessTokenPayload): String {
        val expirationTime = System.currentTimeMillis() + accessTokenLifetime.inWholeMilliseconds
        val jti = Uuid.generateV7().toString()
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("session", payload.sessionId.long)
            .withClaim("client", Json.encodeToString(payload.authState.idOrNull()))
            .withJWTId(jti)
            .withExpiresAt(Instant.ofEpochMilli(expirationTime))
            .sign(algorithm)
    }
}
