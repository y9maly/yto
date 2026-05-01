package y9to.api.types

import kotlinx.serialization.SerialName
import y9to.libs.stdlib.optional.Optional
import kotlinx.serialization.Serializable as S


@S sealed interface ApiUpdateSubscription {
    @SerialName("UserEdited")
    @S data class UserEdited(val user: UserId) : ApiUpdateSubscription

    @SerialName("PostEdited")
    @S data class PostEdited(val post: PostId) : ApiUpdateSubscription
}

@S sealed interface Update {
    @SerialName("AuthStateChanged")
    @S data class AuthStateChanged(val authState: AuthState) : Update

    @SerialName("LoginStateChanged")
    @S data class LoginStateChanged(val loginState: LoginState?) : Update

    @SerialName("UserEdited")
    @S data class UserEdited(val newUser: User) : Update

    @SerialName("PostEdited")
    @S data class PostEdited(
        val post: PostId,
        val newAuthor: Optional<UserId>,
        val newReplyTo: Optional<PostId?>,
        val newContent: Optional<PostContent>,
    ) : Update
}
