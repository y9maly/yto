package domain.service

import backend.core.types.ConfirmCodeDestination
import backend.core.types.FileId
import domain.service.InternalLoginState.ConfirmCodeResult
import domain.service.InternalLoginState.Password2FAResult
import domain.service.InternalLoginState.PreviousStep
import domain.service.InternalLoginState.RegistrationResult
import domain.service.InternalLoginState.Step
import domain.service.InternalLoginState.TelegramOAuthResult
import y9to.libs.stdlib.optional.Optional
import kotlinx.serialization.Serializable as S


@S data class InternalLoginState(
    val initiatedWith: InitiatedWith,
    val previousSteps: List<PreviousStep>,
    val currentStep: Step<*>,

    var confirmedPhoneNumber: String? = null,
    var confirmedEmail: String? = null,
    var confirmedTelegramAuthorizationId: String? = null,
) {
    @S sealed interface InitiatedWith {
        @S data class PhoneNumber(val phoneNumber: String) : InitiatedWith
        @S data class Email(val email: String) : InitiatedWith

        /**
         * TelegramOpenIDConnect
         */
        @S data class TelegramOAuth(val authorizationId: String?) : InitiatedWith
    }

    /**
     * @param stepResult result of this step. For example: true when confirm code was correct.
     */
    @S data class PreviousStep(val step: Step<*>, val stepResult: StepResult) {
        init {
            require(
                (step is Step.Registration && stepResult is RegistrationResult) ||
                (step is Step.ConfirmCode && stepResult is ConfirmCodeResult) ||
                (step is Step.Password2FA && stepResult is Password2FAResult) ||
                (step is Step.TelegramOAuth && stepResult is TelegramOAuthResult)
            )
        }
    }

    @S sealed interface StepResult
    @S sealed interface Step<RESULT : StepResult> {
        @S sealed interface Registration : Step<RegistrationResult> {
            @S data object Default : Registration

            @S data class UsingTelegramOAuthProfileInfo(
                val telegramFirstName: Optional<String>,
                val telegramLastName: Optional<String>,
                val telegramAvatar: Optional<FileId>,
                val telegramPhoneNumber: Optional<String>,
            ) : Registration
        }

        @S data class ConfirmCode(val code: String, val destination: ConfirmCodeDestination) : Step<ConfirmCodeResult>

        @S data object Password2FA : Step<Password2FAResult>

        @S data class TelegramOAuth(
            val isPhoneNumberWasRequested: Boolean,

            val authorizationUri: String,

            /**
             * Random string. OAuth "state".
             * See https://datatracker.ietf.org/doc/html/rfc6749 (4.1.1. Authorization Request)
             */
            val authorizationState: String,

            /**
             * OAuth codeVerifier.
             */
            val authorizationCodeVerifier: String,
        ) : Step<TelegramOAuthResult>
    }

    @S sealed interface RegistrationResult : StepResult {
        @S data object Accepted : RegistrationResult
        @S data object Rejected : RegistrationResult
    }

    @S sealed interface ConfirmCodeResult : StepResult {
        @S data object Valid : ConfirmCodeResult
        @S data object Invalid : ConfirmCodeResult
    }

    @S sealed interface Password2FAResult : StepResult {
        @S data object Valid : Password2FAResult
        @S data object Invalid : Password2FAResult
    }

    @S sealed interface TelegramOAuthResult : StepResult {
        @S data object Valid : TelegramOAuthResult
        @S data object Invalid : TelegramOAuthResult
    }
}

fun InternalLoginState.copy(
    previousStep: PreviousStep,
    nextStep: Step<*>,
) = copy(
    previousSteps = this.previousSteps + previousStep,
    currentStep = nextStep,
)

inline fun <R> PreviousStep.fold(
    registration: (Step.Registration, RegistrationResult) -> R,
    confirmCode: (Step.ConfirmCode, ConfirmCodeResult) -> R,
    password2FA: (Step.Password2FA, Password2FAResult) -> R,
    telegramOAuth: (Step.TelegramOAuth, TelegramOAuthResult) -> R,
): R = when (step) {
    is Step.Registration -> registration(step, stepResult as RegistrationResult)
    is Step.ConfirmCode -> confirmCode(step, stepResult as ConfirmCodeResult)
    is Step.Password2FA -> password2FA(step, stepResult as Password2FAResult)
    is Step.TelegramOAuth ->telegramOAuth(step, stepResult as TelegramOAuthResult)
}

inline fun Step<*>.finalize(
    registration: (Step.Registration) -> RegistrationResult,
    confirmCode: (Step.ConfirmCode) -> ConfirmCodeResult,
    password2FA: (Step.Password2FA) -> Password2FAResult,
    telegramOAuth: (Step.TelegramOAuth) -> TelegramOAuthResult,
): PreviousStep = when (this) {
    is Step.Registration -> finalize(registration(this))
    is Step.ConfirmCode -> finalize(confirmCode(this))
    is Step.Password2FA -> finalize(password2FA(this))
    is Step.TelegramOAuth -> finalize(telegramOAuth(this))
}

fun Step.Registration.finalize(result: RegistrationResult): PreviousStep = PreviousStep(this, result)
fun Step.ConfirmCode.finalize(result: ConfirmCodeResult): PreviousStep = PreviousStep(this, result)
fun Step.Password2FA.finalize(result: Password2FAResult): PreviousStep = PreviousStep(this, result)
fun Step.TelegramOAuth.finalize(result: TelegramOAuthResult): PreviousStep = PreviousStep(this, result)
