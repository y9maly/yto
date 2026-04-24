package presentation.updateSubscriptionsStore

import backend.core.types.PostId
import backend.core.types.UserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S


@S sealed interface UpdateSubscription {
    @SerialName("UserEdited")
    @S data class UserEdited(val user: UserId) : UpdateSubscription

    @SerialName("PostContentEdited")
    @S data class PostContentEdited(val post: PostId) : UpdateSubscription
}
