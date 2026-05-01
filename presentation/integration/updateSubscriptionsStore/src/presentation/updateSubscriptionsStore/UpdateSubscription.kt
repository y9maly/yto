package presentation.updateSubscriptionsStore

import backend.core.types.PostId
import backend.core.types.UserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S


@S sealed interface UpdateSubscription {
    @SerialName("UserEdited")
    @S data class UserEdited(val user: UserId) : UpdateSubscription

    @SerialName("PostEdited")
    @S data class PostEdited(val post: PostId) : UpdateSubscription
}
