package presentation.infra.updateManager

import backend.core.types.SessionId
import y9to.api.types.Update


class UpdateManagerDefault(
    private val updateProducer: UpdateProducer,
    private val updateProvider: UpdateProvider,
) : UpdateManager {
    override suspend fun receive(forSession: SessionId): Update? {
        return updateProvider.receive(forSession)
    }

    override suspend fun await(forSession: SessionId): Update {
        return updateProvider.await(forSession)
    }

    override suspend fun consume(forSession: SessionId) {
        updateProvider.consume(forSession)
    }

    override suspend fun emit(forSession: SessionId, update: Update) {
        updateProducer.emit(forSession, update)
    }
}
