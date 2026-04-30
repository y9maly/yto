package y9to.sdk.services

import kotlinx.coroutines.flow.Flow
import y9to.api.types.LoginState
import y9to.sdk.Client


interface LoginStateService {
    val loginState: Flow<LoginState?>
}

class LoginStateServiceDefault(private val client: Client) : LoginStateService {
    override val loginState = client.auth.loginState
}
