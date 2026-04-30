package y9to.sdk.services

import y9to.api.types.LogOutResult
import y9to.sdk.Client


interface LogoutService {
    suspend fun logOut(): LogOutResult
}

class LogoutServiceDefault(private val client: Client) : LogoutService {
    override suspend fun logOut(): LogOutResult {
        return client.auth.logOut()
    }
}
