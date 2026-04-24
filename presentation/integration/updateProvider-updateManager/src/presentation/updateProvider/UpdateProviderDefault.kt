package presentation.updateProvider

import backend.core.types.SessionId
import presentation.infra.updateManager.UpdateManager
import y9to.api.types.Update


class UpdateProviderDefault(
    private val updateManager: UpdateManager,
) : UpdateProvider {
    override suspend fun receive(forSession: SessionId): Update? {
        return updateManager.receive(forSession)
    }

    override suspend fun await(forSession: SessionId): Update {
        return updateManager.await(forSession)
    }

    override suspend fun consume(forSession: SessionId) {
        updateManager.consume(forSession)
    }
}
