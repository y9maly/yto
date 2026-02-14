package presentation.integration.context.elements

import backend.core.types.AuthState
import backend.core.types.SessionId
import presentation.integration.context.Context
import presentation.integration.context.Context.Keys
import y9to.libs.stdlib.delegates.static


val Keys.SessionId by static { Context.Key<SessionId>() }
val Keys.AuthState by static { Context.Key<AuthState>() }

context(it: Context)
var sessionId
    get() = it.contextMap[Keys.SessionId]
    set(value) { it.contextMap.set(Keys.SessionId, value) }

context(it: Context)
var authState
    get() = it.contextMap[Keys.AuthState]
    set(value) { it.contextMap.set(Keys.AuthState, value) }

context(it: Context)
inline fun authStateOrPut(value: () -> AuthState) =
    if (Keys.AuthState in it.contextMap) authState
    else value().also { authState = it }

