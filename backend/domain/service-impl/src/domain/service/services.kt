@file:OptIn(InterfaceClass::class)

package domain.service

import domain.selector.MainSelector
import integration.fileStorage.FileStorage
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

fun FileServiceImpl(
    repo: MainRepository,
    fileStorage: FileStorage,
    clock: Clock,
) = FileService(repo, fileStorage, clock)
