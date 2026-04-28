package y9to.api.y9to.api.endpoint

import y9to.api.types.*


interface AuthEndpoint {
    fun createSession(): Token

    // just until update system starts working
    fun needResetLocalCache(): Boolean

    fun getSession(): Session
    fun getAuthState(): AuthState
    fun logOut(): LogOutResult
}
