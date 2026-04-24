package domain.event

import backend.core.types.AuthState
import backend.core.types.Session
import backend.core.types.SessionId
import kotlinx.serialization.Serializable as S


@S data class SessionCreated(val session: Session) : Event

@S data class AuthStateChanged(val session: SessionId, val authState: AuthState) : Event
