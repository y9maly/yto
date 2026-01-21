package presentation.integration.callContext.elements

import backend.core.types.AuthState
import backend.core.types.SessionId
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.CallContext.Keys
import y9to.libs.stdlib.delegates.static


val Keys.SessionId by static { CallContext.Key<SessionId>() }
val Keys.AuthState by static { CallContext.Key<AuthState>() }

var CallContext.sessionId
    get() = contextMap[Keys.SessionId]
    set(value) { contextMap.set(Keys.SessionId, value) }

var CallContext.authState
    get() = contextMap[Keys.AuthState]
    set(value) { contextMap.set(Keys.AuthState, value) }

inline fun CallContext.authStateOrPut(value: () -> AuthState) =
    if (Keys.AuthState in contextMap) authState
    else value().also { authState = it }
