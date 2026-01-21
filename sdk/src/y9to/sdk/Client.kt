package y9to.sdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import y9to.api.krpc.MainRpc
import y9to.api.types.Session
import y9to.api.types.SessionId
import y9to.api.types.Token


class Client internal constructor(
    token: Token,
    initialSession: Session,
    internal val scope: CoroutineScope,
    internal val rpc: MainRpc,
) {
    internal val token get() = auth.token
    val auth = AuthClient(this, initialSession, token)
    val user = UserClient(this)
}


suspend fun createRpcSdkClient(
    rpc: MainRpc
): Client {
    val token = Token(Token.Unsafe(SessionId(2), "0.0.1"))
    val session = rpc.auth.getSession(token)
    val scope = CoroutineScope(SupervisorJob())
    return Client(token, session, scope, rpc)
}
