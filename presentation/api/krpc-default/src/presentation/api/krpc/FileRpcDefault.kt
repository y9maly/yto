package presentation.api.krpc

import presentation.api.krpc.internals.authenticate
import presentation.authenticator.Authenticator
import presentation.integration.context.Context
import y9to.api.controller.FileController
import y9to.api.krpc.FileRpc
import y9to.api.types.FileId
import y9to.api.types.FileTypes
import y9to.api.types.Token


class FileRpcDefault(
    private val authenticator: Authenticator,
    private val controller: FileController,
) : FileRpc {
    override suspend fun get(token: Token, id: FileId) =
        authenticate(token) { get(id) }

    override suspend fun upload(
        token: Token,
        name: String,
        types: FileTypes?,
        expectedSize: Long?,
    ) = authenticate(token) { upload(name, types, expectedSize) }

    override suspend fun download(token: Token, id: FileId) =
        authenticate(token) { download(id) }

    private suspend inline fun <R> authenticate(token: Token, block: context(Context) FileController.() -> R) =
        authenticate(authenticator, token) { block(this, controller) }
}
