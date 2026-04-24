package y9to.sdk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import y9to.sdk.internals.ClientOwner
import y9to.sdk.internals.request
import kotlin.coroutines.cancellation.CancellationException


class UserClient internal constructor(override val client: Client) : ClientOwner {
    val myProfile: Flow<MyProfile?> = channelFlow {
        client.auth.authState.collectLatest { authState ->
            if (authState !is AuthState.Authorized)
                return@collectLatest

            var profile = request { rpc.user.getMyProfile(token) }
                ?: return@collectLatest
            send(profile)

            client.updateCenter.updates.filterIsInstance<Update.UserEdited>().collect { update ->
                val user = update.newUser
                if (user.id != profile.id)
                    return@collect

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

    suspend fun get(id: UserId) = get(InputUser.Id(id))
    suspend fun get(input: InputUser): User? {
        return request { rpc.user.get(token, input) }
    }

    fun getFlow(id: UserId) = getFlow(InputUser.Id(id))
    fun getFlow(input: InputUser): Flow<User?> = flow {
        while (true) {
            try {
                emit(get(input))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            } finally {
                delay((1500..3000L).random())
            }
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
