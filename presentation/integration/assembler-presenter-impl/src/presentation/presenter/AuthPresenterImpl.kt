package presentation.presenter

import domain.service.ServiceCollection
import presentation.integration.context.Context
import presentation.integration.context.elements.sessionId
import presentation.mapper.map
import presentation.presenter.map
import y9to.api.types.*
import y9to.api.types.LoginState.Registration
import backend.core.types.AuthState as BackendAuthState
import backend.core.types.Session as BackendSession


class AuthPresenterImpl(
    private val service: ServiceCollection,
) : AuthPresenter {
    context(context: Context)
    override suspend fun Session(backendSession: BackendSession): Session {
        val isSelf = backendSession.id == sessionId

        if (!isSelf)
            error("Session ${backendSession.id} cannot be present to session $sessionId")

        return Session(
            id = backendSession.id.map(),
            creationDate = backendSession.creationDate,
        )
    }

    context(context: Context)
    override suspend fun AuthState(backendAuthState: BackendAuthState): AuthState {
        return when (backendAuthState) {
            is BackendAuthState.Authorized -> AuthState.Authorized(backendAuthState.id.map())
            is BackendAuthState.Unauthorized -> AuthState.Unauthorized
        }
    }

    context(context: Context)
    override suspend fun LoginState(
        backendLoginState: backend.core.types.LoginState
    ): LoginState = backendLoginState.internalMap()
}

private fun backend.core.types.LoginState.internalMap(): LoginState = when (this) {
    is backend.core.types.LoginState.ConfirmCode -> LoginState.ConfirmCode(
        digitsOnly = digitsOnly,
        length = length,
        destination = when (val destination = destination) {
            is backend.core.types.ConfirmCodeDestination.Phone -> ConfirmCodeDestination.Phone(destination.phoneNumber)
            is backend.core.types.ConfirmCodeDestination.Email -> ConfirmCodeDestination.Email(destination.email)
        },
    )

    is backend.core.types.LoginState.Password2FA -> LoginState.Password2FA(
        hint = hint,
    )

    is backend.core.types.LoginState.OAuthInProgress -> LoginState.OAuthInProgress(
        sessionInfo = when (val sessionInfo = sessionInfo) {
            is backend.core.types.OAuthSessionInfo.Telegram -> OAuthSessionInfo.Telegram(
                isPhoneNumberWasRequested = sessionInfo.isPhoneNumberWasRequested,
            )
        },
        authorizationUri = authorizationUri,
    )

    is backend.core.types.LoginState.Registration -> Registration(
        preFilledRegistrationFields = PreFilledRegistrationFields(
            firstName = preFilledRegistrationFields.firstName?.map(),
            lastName = preFilledRegistrationFields.lastName?.map(),
            bio = preFilledRegistrationFields.bio?.map(),
            birthday = preFilledRegistrationFields.birthday?.map(),
            avatar = null, // todo
            cover = null,
        ),
        linkPhoneNumberInfo = when (val linkPhoneNumberInfo = linkPhoneNumberInfo) {
            backend.core.types.LinkPhoneNumberInfo.None -> LinkPhoneNumberInfo.None
            is backend.core.types.LinkPhoneNumberInfo.Optional -> LinkPhoneNumberInfo.Optional(linkPhoneNumberInfo.phoneNumber)
            is backend.core.types.LinkPhoneNumberInfo.Mandatory -> LinkPhoneNumberInfo.Mandatory(linkPhoneNumberInfo.phoneNumber)
            is backend.core.types.LinkPhoneNumberInfo.Restricted -> LinkPhoneNumberInfo.Restricted(linkPhoneNumberInfo.phoneNumber, when (linkPhoneNumberInfo.reason) {
                backend.core.types.LinkPhoneNumberInfo.RestrictedReason.OtherUserThisThisPhoneNumberExists -> LinkPhoneNumberInfo.RestrictedReason.OtherUserThisThisPhoneNumberExists
                backend.core.types.LinkPhoneNumberInfo.RestrictedReason.Other -> LinkPhoneNumberInfo.RestrictedReason.Other
            })
        },
        linkEmailInfo = when (val linkEmailInfo = linkEmailInfo) {
            backend.core.types.LinkEmailInfo.None -> LinkEmailInfo.None
            is backend.core.types.LinkEmailInfo.Optional -> LinkEmailInfo.Optional(linkEmailInfo.email)
            is backend.core.types.LinkEmailInfo.Mandatory -> LinkEmailInfo.Mandatory(linkEmailInfo.email)
        }
    )
}

private fun <T> backend.core.types.PreFilledRegistrationField<T>.map() = PreFilledRegistrationField(value, source.map())
private fun backend.core.types.PreFilledRegistrationFieldSource.map() = when (this) {
    backend.core.types.PreFilledRegistrationFieldSource.Telegram -> PreFilledRegistrationFieldSource.Telegram
    backend.core.types.PreFilledRegistrationFieldSource.Other -> PreFilledRegistrationFieldSource.Other
}
