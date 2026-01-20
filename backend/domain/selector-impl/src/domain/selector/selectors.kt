package domain.selector

import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass


@OptIn(InterfaceClass::class)
fun PostSelectorImpl(
    repository: MainRepository,
) = PostSelector(repository)

@OptIn(InterfaceClass::class)
fun UserSelectorImpl(
    repository: MainRepository,
) = UserSelector(repository)
