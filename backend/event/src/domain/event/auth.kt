package domain.event

import backend.core.types.AuthState
import backend.core.types.SessionId


data class AuthStateChanged(val session: SessionId, val authState: AuthState) : Event
