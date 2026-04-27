package integration.loginRepository

import backend.core.types.SessionId
import domain.service.InternalLoginState
import kotlin.time.Duration


interface LoginRepository {
    suspend fun saveLoginState(session: SessionId, state: InternalLoginState, ttl: Duration)
    suspend fun getLoginState(session: SessionId): InternalLoginState?
    suspend fun resetLoginState(session: SessionId)
}
