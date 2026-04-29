package backend.core.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as S
import y9to.libs.stdlib.optional.Optional


@S data class LoginCapabilities(
    val availableLoginMethods: Set<LoginMethod>,
    val requiredToLinkPhoneNumberWhileTelegramOAuthRegistration: Boolean,
)

@S sealed interface LoginState {
    @SerialName("ConfirmCode")
    @S data class ConfirmCode(
        val digitsOnly: Boolean,
        val length: Int,
        val destination: ConfirmCodeDestination,
    ) : LoginState

    @SerialName("Password2FA")
    @S data class Password2FA(
        val hint: String?,
    ) : LoginState

    @SerialName("OAuthInProgress")
    @S data class OAuthInProgress(
        val sessionInfo: OAuthSessionInfo,
        val authorizationUri: String,
    ) : LoginState

    @SerialName("Registration")
    @S data class Registration(
        val preFilledRegistrationFields: PreFilledRegistrationFields,
        val linkPhoneNumberInfo: LinkPhoneNumberInfo,
        val linkEmailInfo: LinkEmailInfo,
    ) : LoginState
}

// Start

@S sealed interface LoginMethod {
    @S data object Phone : LoginMethod
    @S data object Email : LoginMethod
    @S data object TelegramOAuth : LoginMethod
}

// Confirm code & Password2FA

@S sealed interface ConfirmCodeDestination {
    @S data class Phone(val phoneNumber: String) : ConfirmCodeDestination
    @S data class Email(val email: String) : ConfirmCodeDestination
}

// OAuth

@S sealed interface OAuthProvider {
    @S data object Telegram : OAuthProvider
}

@S sealed interface OAuthSessionInfo {
    val provider: OAuthProvider

    @S data class Telegram(
        val isPhoneNumberWasRequested: Boolean,
    ) : OAuthSessionInfo{
        override val provider = OAuthProvider.Telegram
    }
}

// Registration

@S data class PreFilledRegistrationFields(
    val firstName: PreFilledRegistrationField<String>? = null,
    val lastName: PreFilledRegistrationField<String>? = null,
    val bio: PreFilledRegistrationField<String>? = null,
    val birthday: PreFilledRegistrationField<String>? = null,
    val avatar: PreFilledRegistrationField<FileId>? = null,
    val cover: PreFilledRegistrationField<FileId>? = null,
)

@S data class PreFilledRegistrationField<out VALUE>(
    val value: VALUE,
    val source: PreFilledRegistrationFieldSource,
)

@S sealed interface PreFilledRegistrationFieldSource {
    @S data object Telegram : PreFilledRegistrationFieldSource
    @S data object Other : PreFilledRegistrationFieldSource
}

@S sealed interface LinkPhoneNumberInfo {
    @S data object None : LinkPhoneNumberInfo
    @S data class Optional(val phoneNumber: String) : LinkPhoneNumberInfo
    @S data class Mandatory(val phoneNumber: String) : LinkPhoneNumberInfo
    @S data class Restricted(val phoneNumber: String, val reason: RestrictedReason) : LinkPhoneNumberInfo
    @S enum class RestrictedReason {
        OtherUserThisThisPhoneNumberExists, Other
    }
}

@S sealed interface LinkEmailInfo {
    @S data object None : LinkEmailInfo
    @S data class Optional(val email: String) : LinkEmailInfo
    @S data class Mandatory(val email: String) : LinkEmailInfo
}
