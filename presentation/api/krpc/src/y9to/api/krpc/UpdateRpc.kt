package y9to.api.krpc

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import y9to.api.types.ApiUpdateSubscription
import y9to.api.types.Token
import y9to.api.types.Update


@Rpc
interface UpdateRpc {
    fun receive(token: Token): Flow<Update>
    suspend fun subscribe(token: Token, subscription: ApiUpdateSubscription)
    suspend fun unsubscribe(token: Token, subscription: ApiUpdateSubscription)
}
