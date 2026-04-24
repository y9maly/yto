package presentation.infra.updateManager

import backend.core.types.SessionId
import y9to.api.types.Update


interface UpdateProvider {
    suspend fun receive(forSession: SessionId): Update?
    suspend fun await(forSession: SessionId): Update
    suspend fun consume(forSession: SessionId)
}
