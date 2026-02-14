package presentation.api.krpc

import presentation.api.krpc.internals.authenticate
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import y9to.api.controller.UserController
import y9to.api.krpc.UserRpc
import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.libs.stdlib.optional.Optional


class UserRpcDefault(
    private val authenticator: Authenticator,
    private val controller: UserController,
) : UserRpc {
    override suspend fun getMyProfile(token: Token): MyProfile? =
        authenticate(token) { getMyProfile() }

    override suspend fun get(token: Token, input: InputUser) =
        authenticate(token) { get(input) }

    override suspend fun editMe(
        token: Token,
        firstName: Optional<String>,
        lastName: Optional<String?>,
        bio: Optional<String?>,
        birthday: Optional<Birthday?>,
        cover: Optional<FileId?>,
        avatar: Optional<FileId?>,
    ) = authenticate(token) {
        editMe(
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        )
    }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context) UserController.() -> R) =
        authenticate(authenticator, token) { block(this, controller) }
}
