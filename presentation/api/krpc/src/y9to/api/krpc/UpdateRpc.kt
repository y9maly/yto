package y9to.api.krpc

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import y9to.api.types.Token
import y9to.api.types.Update


@Rpc
interface UpdateRpc {
    fun receive(token: Token): Flow<Update>
}
