package y9to.sdk

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import y9to.api.types.AuthState
import y9to.api.types.EditMeResult
import y9to.api.types.InputUser
import y9to.common.types.Birthday
import y9to.libs.stdlib.coroutines.mapIn
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import kotlin.coroutines.cancellation.CancellationException


class UserClient internal constructor(private val client: Client) {
    val myProfile = channelFlow {
        while (true) {
            try {
                val myProfile = client.rpc.user.getMyProfile(client.token)
                send(myProfile)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            }

            delay((2000L..3000L).random())
        }
    }
        .distinctUntilChanged()
        .shareIn(client.scope, SharingStarted.WhileSubscribed(5000), 1)

    suspend fun editMe(
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
    ): EditMeResult {
        return client.rpc.user.editMe(
            token = client.token,
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
        )
    }
}
