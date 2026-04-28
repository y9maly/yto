package integration.repository

import integration.repository.internals.FirstRevision
import integration.repository.result.LogInError
import integration.repository.result.LogInOk
import integration.repository.result.LogInResult
import integration.repository.result.LogOutError
import integration.repository.result.LogOutOk
import integration.repository.result.LogOutResult
import backend.infra.postgres.table.TAuthState
import backend.infra.postgres.table.TSession
import backend.core.types.AuthState
import backend.core.types.ClientId
import backend.core.types.Session
import backend.core.types.SessionId
import backend.core.types.UserId
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import kotlin.time.Instant


internal class PostgresAuthRepository(private val main: MainRepository) : AuthRepository {
    override suspend fun createSession(creationDate: Instant): Session = main.transaction {
        val id = TSession.insertAndGetId {
            it[creation_date] = creationDate
        }.value

        Session(
            id = SessionId(id),
            revision = FirstRevision,
            creationDate = creationDate,
        )
    }

    override suspend fun getSession(id: SessionId): Session? = main.transaction(ReadOnly) {
        val row = TSession
            .selectAll()
            .where { TSession.id eq id.long }
            .limit(1)
            .firstOrNull() ?: return@transaction null
        Session(
            id = SessionId(row[TSession.id].value),
            revision = row[TSession.revision],
            creationDate = row[TSession.creation_date],
        )
    }

    override suspend fun existsSession(id: SessionId): Boolean = main.transaction(ReadOnly) {
        TSession
            .select(intLiteral(1))
            .where { TSession.id eq id.long }
            .limit(1)
            .firstOrNull() != null
    }

    override suspend fun getAuthState(session: SessionId): AuthState? = main.transaction {
        val row = TAuthState.select(TAuthState.user)
            .where { TAuthState.session eq session.long }
            .limit(1)
            .firstOrNull()
        if (row == null) {
            if (existsSession(session))
                return@transaction AuthState.Unauthorized
            return@transaction null
        }
        return@transaction AuthState.Authorized(UserId(row[TAuthState.user]))
    }

    override suspend fun logIn(session: SessionId, client: ClientId): LogInResult = main.transaction {
        when (getAuthState(session)) {
            is AuthState.Unauthorized -> { /* ok */ }
            is AuthState.Authorized -> return@transaction LogInError.AlreadyAuthenticated.asError()
            null -> return@transaction LogInError.InvalidSessionId.asError()
        }

        when (client) {
            is UserId -> {
                if (!main.user.exists(client)) {
                    return@transaction LogInError.InvalidClientId.asError()
                }
            }
        }

        TAuthState.insert {
            it[TAuthState.session] = session.long
            it[TAuthState.user] = client.long
        }

        LogInOk.asOk()
    }

    override suspend fun logOut(session: SessionId): LogOutResult = main.transaction {
        val oldAuthState = when (val it = getAuthState(session)) {
            is AuthState.Authorized -> it
            is AuthState.Unauthorized -> return@transaction LogOutError.AlreadyLogOuted.asError()
            null -> return@transaction LogOutError.InvalidSessionId.asError()
        }

        val deletedRows = TAuthState.deleteWhere { TAuthState.session eq session.long }

        if (deletedRows == 0) {
            if (existsSession(session)) {
                return@transaction LogOutError.AlreadyLogOuted.asError()
            } else {
                return@transaction LogOutError.InvalidSessionId.asError()
            }
        }

        LogOutOk.asOk()
    }

    override suspend fun getAuthenticatedSessions(client: ClientId): Set<SessionId> = main.transaction(ReadOnly) {
        TAuthState.select(TAuthState.session)
            .where {
                when (client) {
                    is UserId -> TAuthState.user eq client.long
                }
            }
            .map { SessionId(it[TAuthState.session]) }
            .toSet()
    }
}
