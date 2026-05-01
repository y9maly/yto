package presentation.updateSubscriptionsStore

import backend.core.types.PostId
import backend.core.types.UserId


sealed interface UpdateEvent {
    data class UserEdited(val user: UserId) : UpdateEvent
    data class PostEdited(val post: PostId) : UpdateEvent
}
