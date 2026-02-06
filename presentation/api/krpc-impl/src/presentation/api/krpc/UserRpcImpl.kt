package presentation.api.krpc

import backend.core.reference.UserReference
import domain.service.MainService
import domain.service.result.EditUserAvatarError
import domain.service.result.EditUserBioError
import domain.service.result.EditUserBirthdayError
import domain.service.result.EditUserCoverError
import domain.service.result.EditUserError
import domain.service.result.EditUserNameError
import presentation.api.krpc.internals.authenticate
import presentation.assembler.MainAssembler
import presentation.authenticator.Authenticator
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.authStateOrPut
import presentation.integration.callContext.elements.sessionId
import presentation.presenter.MainPresenter
import y9to.api.krpc.UserRpc
import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.map


class UserRpcImpl(
    private val authenticator: Authenticator,
    private val service: MainService,
    private val assembler: MainAssembler,
    private val presenter: MainPresenter,
) : UserRpc {
    override suspend fun getMyProfile(token: Token): MyProfile? = authenticate(token) {
        val userId = authStateOrPut {
            service.auth.getAuthState(sessionId) ?: return@authenticate null
        }.userIdOrNull() ?: return@authenticate null
        val user = service.user.get(userId) ?: return@authenticate null
        presenter.user.MyProfile(user)
    }

    override suspend fun get(token: Token, input: InputUser): User? = authenticate(token) {
        val userRef = assembler.user.resolve(input)
            ?: return null
        val user = service.user.get(userRef)
            ?: return null
        val remoteUser = presenter.user.User(user)
        remoteUser
    }

    override suspend fun editMe(
        token: Token,
        firstName: Optional<String>,
        lastName: Optional<String?>,
        bio: Optional<String?>,
        birthday: Optional<Birthday?>,
        cover: Optional<FileId?>,
        avatar: Optional<FileId?>,
    ): EditMeResult = authenticate(token) {
        val userId = authStateOrPut {
            service.auth.getAuthState(sessionId) ?: return@authenticate EditMeError.Unauthenticated.asError()
        }.userIdOrNull() ?: return@authenticate EditMeError.Unauthenticated.asError()

        if (arrayOf(
            firstName,
            lastName,
            bio,
            birthday,
            cover,
            avatar,
        ).all { it.isNone }) {
            return@authenticate EditMeError.NothingToChange.asError()
        }

        val cover = cover.map { assembler.file.FileId(it ?: return@map null) }
        val avatar = avatar.map { assembler.file.FileId(it ?: return@map null) }

        service.user.edit(
            ref = UserReference.Id(userId),
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        ).onError { error ->
            return@authenticate when (error) {
                is EditUserError.UnknownUserReference -> error("Unreachable")
                is EditUserError.FieldErrors -> error.map().asError()
            }
        }

        Unit.asOk()
    }

    private suspend inline fun <R> authenticate(token: Token, block: CallContext.() -> R) =
        authenticate(authenticator, token, block)
}

private fun EditUserError.FieldErrors.map() = EditMeError.FieldErrors(
    firstNameError = when (firstNameError) {
        null -> null
        is EditUserNameError.CannotBeBlank -> EditNameError.CannotBeBlank
        is EditUserNameError.ExceededLengthRange -> EditNameError.ExceededLengthRange
    },
    lastNameError = when (lastNameError) {
        null -> null
        is EditUserNameError.CannotBeBlank -> EditNameError.CannotBeBlank
        is EditUserNameError.ExceededLengthRange -> EditNameError.ExceededLengthRange
    },
    bioError = when (bioError) {
        null -> null
        is EditUserBioError.ExceededLengthRange -> EditBioError.ExceededLengthRange
    },
    birthdayError = when (birthdayError) {
        null -> null
        is EditUserBirthdayError.ExceededDateRange -> EditBirthdayError.ExceededDateRange
    },
    coverError = when (coverError) {
        null -> null
        is EditUserCoverError.InvalidFile -> EditCoverError.InvalidFile
    },
    avatarError = when (avatarError) {
        null -> null
        is EditUserAvatarError.InvalidFile -> EditAvatarError.InvalidFile
    },
)
