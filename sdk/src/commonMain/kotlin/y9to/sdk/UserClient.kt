package y9to.sdk

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import y9to.api.types.EditMeError
import y9to.api.types.EditMeResult
import y9to.api.types.FileId
import y9to.api.types.InputUser
import y9to.api.types.MyProfile
import y9to.api.types.User
import y9to.api.types.UserId
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.coroutines.flow.collectLatestIn
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import kotlin.coroutines.cancellation.CancellationException


class UserClient internal constructor(private val client: Client) {
    private val _myProfile = MutableSharedFlow<MyProfile?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val myProfile: Flow<MyProfile?> = _myProfile.distinctUntilChanged()

    init {
        val myProfileDemand = SharingStarted.WhileSubscribed(5000)
            .command(_myProfile.subscriptionCount)
            .map { it == SharingCommand.START }

        myProfileDemand.collectLatestIn(client.scope) { demand ->
            if (!demand)
                return@collectLatestIn

            while (true) {
                try {
                    val value = client.rpc.user.getMyProfile(client.token)
                    check(_myProfile.tryEmit(value))
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    e.printStackTrace()
                } finally {
                    delay((2000L..3000L).random())
                }
            }
        }
    }

    suspend fun get(id: UserId) = get(InputUser.Id(id))
    suspend fun get(input: InputUser): User? {
        return client.rpc.user.get(client.token, input)
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

        return client.rpc.user.editMe(
            token = client.token,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        )
            .onSuccess {
                val value = client.rpc.user.getMyProfile(client.token)
                check(_myProfile.tryEmit(value))
            }
    }
}
