package y9to.sdk.internals

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import y9to.api.types.Update
import y9to.sdk.Client
import kotlin.time.Duration.Companion.milliseconds


internal class UpdateCenter(
    override val client: Client,
) : ClientOwner {
    private val _updates = MutableSharedFlow<Update>(extraBufferCapacity = Int.MAX_VALUE)
    val updates = _updates.asSharedFlow()

    init {
        client.scope.launch {
            while (true) {
                try {
                    request {
                        rpc.update.receive(token).collect {
                            check(_updates.tryEmit(it))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(1000.milliseconds)
                    continue
                }
            }
        }
    }
}
