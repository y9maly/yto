@file:OptIn(InterfaceClass::class)

package domain.service

import domain.selector.MainSelector
import integration.repository.MainRepository
import y9to.libs.stdlib.InterfaceClass
import kotlin.time.Clock


fun AuthServiceImpl(
    repo: MainRepository,
    clock: Clock,
) = AuthService(repo, clock)

fun UserServiceImpl(
    repo: MainRepository,
    selector: MainSelector,
    clock: Clock,
) = UserService(repo, selector, clock)

fun PostServiceImpl(
    repo: MainRepository,
    selector: MainSelector,
    clock: Clock,
) = PostService(repo, selector, clock)
