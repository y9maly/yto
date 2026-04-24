package presentation.infra.updateManager

import backend.core.types.SessionId
import y9to.api.types.Update


interface UpdateProducer {
    suspend fun emit(forSession: SessionId, update: Update)
}
