package y9to.sdk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.coroutines.flow.collectIn
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import y9to.sdk.internals.ClientOwner
import y9to.sdk.internals.request
import kotlin.coroutines.cancellation.CancellationException


class UserClient internal constructor(override val client: Client) : ClientOwner {
    val myProfile: Flow<MyProfile?> = channelFlow {
        client.auth.authState.collectLatest { authState ->
            if (authState !is AuthState.Authorized) {
                send(null)
                return@collectLatest
            }

            var profile = request { rpc.user.getMyProfile(token) }
                ?: run {
                    send(null)
                    return@collectLatest
                }
            send(profile)

            client.updateCenter.updates.filterIsInstance<Update.AuthStateChanged>().collectIn(this) { update ->
                when (update.authState) {
                    is AuthState.Authorized -> {
                        profile = request { rpc.user.getMyProfile(token) }
                            ?: run {
                                send(null)
                                return@collectIn
                            }
                        send(profile)
                    }

                    AuthState.Unauthorized -> {
                        send(null)
                    }
                }
            }

            client.updateCenter.updates.filterIsInstance<Update.UserEdited>().collectIn(this) { update ->
                val user = update.newUser
                if (user.id != profile.id)
                    return@collectIn

                send(MyProfile(
                    id = user.id,
                    registrationDate = user.registrationDate,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    phoneNumber = user.phoneNumber,
                    email = user.email,
                    birthday = user.birthday,
                    bio = user.bio,
                    cover = user.cover,
                    avatar = user.avatar,
                ))
            }
        }
    }
        .distinctUntilChanged()
        .shareIn(client.scope, SharingStarted.WhileSubscribed(5000), 1)

    suspend fun get(input: InputUser): User? {
        return request { rpc.user.get(token, input) }
    }

    fun getFlow(input: InputUser): Flow<User?> = channelFlow {
        var user = request { rpc.user.get(token, input) }
            ?: run {
                send(null)
                return@channelFlow
            }
        send(user)

        client.updateCenter.subscribe(ApiUpdateSubscription.UserEdited(user.id))

        try {
            client.updateCenter.updates.filterIsInstance<Update.UserEdited>().collect { update ->
                if (update.newUser.id != user.id)
                    return@collect
                user = update.newUser
                send(user)
            }
        } finally {
            client.updateCenter.unsubscribe(ApiUpdateSubscription.UserEdited(user.id))
        }
    }

    suspend fun editMe(
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
        cover: Optional<FileId?> = none(),
        avatar: Optional<FileId?> = none(),
    ): EditMeResult {
        if (arrayOf(
            firstName,
            lastName,
            bio,
            birthday,
            cover,
            avatar,
        ).all { it.isNone }) {
            return EditMeError.NothingToChange.asError()
        }

        return request {
            rpc.user.editMe(
                token = token,
                firstName = firstName,
                lastName = lastName,
                bio = bio,
                birthday = birthday,
                cover = cover,
                avatar = avatar,
            )
        }
    }
}
