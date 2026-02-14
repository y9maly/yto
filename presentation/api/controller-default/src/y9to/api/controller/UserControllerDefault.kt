package y9to.api.controller

import backend.core.types.UserReference
import domain.service.MainService
import domain.service.result.*
import presentation.assembler.MainAssembler
import presentation.assembler.map
import presentation.assembler.resolve
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.presenter.MainPresenter
import presentation.presenter.mapAsMyProfile
import presentation.presenter.mapAsUser
import y9to.api.types.*
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.map


class UserControllerDefault(
    private val service: MainService,
    override val assembler: MainAssembler,
    override val presenter: MainPresenter,
) : UserController, ControllerDefault {
    context(_: Context)
    override suspend fun getMyProfile(): MyProfile? = context {
        val userId = authStateOrPut {
            service.auth.getAuthState(sessionId) ?: return null
        }.userIdOrNull() ?: return null
        val user = service.user.get(userId) ?: return null
        return user.mapAsMyProfile()
    }

    context(_: Context)
    override suspend fun get(input: InputUser): User? = context {
        val userRef = input.resolve()
            ?: return null
        val user = service.user.get(userRef)
            ?: return null
        return user.mapAsUser()
    }

    context(_: Context)
    override suspend fun editMe(
        firstName: Optional<String>,
        lastName: Optional<String?>,
        bio: Optional<String?>,
        birthday: Optional<Birthday?>,
        cover: Optional<FileId?>,
        avatar: Optional<FileId?>
    ): EditMeResult = context {
        val userId = authStateOrPut {
            service.auth.getAuthState(sessionId) ?: return EditMeError.Unauthenticated.asError()
        }.userIdOrNull() ?: return EditMeError.Unauthenticated.asError()

        if (arrayOf(
            firstName,
            lastName,
            bio,
            birthday,
            cover,
            avatar,
        ).all { it.isNone }) {
            return EditMeError.NothingToChange.asError()
        }

        val cover = cover.map { it?.map() }
        val avatar = avatar.map { it?.map() }

        service.user.edit(
            ref = UserReference.Id(userId),
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        ).onError { error ->
            return when (error) {
                is EditUserError.UnknownUserReference -> error("Unreachable")
                is EditUserError.FieldErrors -> error.map().asError()
            }
        }

        return Unit.asOk()
    }
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
