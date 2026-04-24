package domain.event

import backend.core.types.User
import kotlinx.serialization.Serializable as S


@S data class UserRegistered(val user: User) : Event

@S data class UserEdited(val user: User) : Event
