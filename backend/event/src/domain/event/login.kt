package domain.event

import backend.core.types.LoginState
import backend.core.types.SessionId
import kotlinx.serialization.Serializable as S


@S data class LoginStateChanged(val session: SessionId, val loginState: LoginState?) : Event
