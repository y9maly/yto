package y9to.api.controller

import kotlinx.coroutines.flow.Flow
import presentation.integration.context.Context
import y9to.api.types.ApiUpdateSubscription
import y9to.api.types.Token
import y9to.api.types.Update


interface UpdateController {
    context(_: Context) suspend fun await(): List<Update>
    context(_: Context) suspend fun consume(count: Int)
    context(_: Context) suspend fun subscribe(subscription: ApiUpdateSubscription)
    context(_: Context) suspend fun unsubscribe(subscription: ApiUpdateSubscription)
}
