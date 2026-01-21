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
import y9to.api.types.InputUser
import y9to.libs.stdlib.coroutines.mapIn
import kotlin.coroutines.cancellation.CancellationException


class UserClient internal constructor(private val client: Client) {
    val me = channelFlow {
        val authState = client.auth.authState.stateIn(this)
        val selfUserIdFlow = authState.mapIn(this) { it.userIdOrNull() }

        while (true) {
            val selfUserId = selfUserIdFlow.value

            try {
                if (selfUserId != null) {
                    send(client.rpc.user.get(client.token, InputUser.Id(selfUserId)))
                } else {
                    send(null)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            }

            delay((2000L..3000L).random())
        }
    }
        .distinctUntilChanged()
        .shareIn(client.scope, SharingStarted.WhileSubscribed(5000), 1)
}
