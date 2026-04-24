package presentation.workers.updatePublisher

import presentation.presenter.PresenterCollection
import presentation.updateProducer.UpdateProducer


interface UpdatePublisher {
    suspend fun start(): Nothing
}
