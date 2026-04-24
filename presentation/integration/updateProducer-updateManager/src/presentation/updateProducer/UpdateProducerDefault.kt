package presentation.updateProducer

import backend.core.types.SessionId
import presentation.infra.updateManager.UpdateManager
import y9to.api.types.Update


class UpdateProducerDefault(
    private val updateManager: UpdateManager,
) : UpdateProducer {
    override suspend fun emit(forSession: SessionId, update: Update) {
        updateManager.emit(forSession, update)
    }
}
