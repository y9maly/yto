package domain.service

import backend.core.types.File
import domain.service.InternalLoginState.ConfirmCodeResult
import domain.service.InternalLoginState.ConfirmPasswordResult
import domain.service.InternalLoginState.PreviousStep
import domain.service.InternalLoginState.RegistrationResult
import domain.service.InternalLoginState.Step
import domain.service.InternalLoginState.TelegramOIDCResult
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
        @S data object TelegramOIDC : InitiatedWith
    }

    /**
     * @param stepResult result of this step. For example: true when confirm code was correct.
     */
    @S data class PreviousStep(val step: Step<*>, val stepResult: StepResult) {
        init {
            require(
                (step is Step.Registration && stepResult is RegistrationResult) ||
                (step is Step.ConfirmCode && stepResult is ConfirmCodeResult) ||
                (step is Step.ConfirmPassword && stepResult is ConfirmPasswordResult) ||
                (step is Step.TelegramOIDC && stepResult is TelegramOIDCResult)
            )
        }
    }

    @S sealed interface StepResult
    @S sealed interface Step<RESULT : StepResult> {
        @S sealed interface Registration : Step<RegistrationResult> {
            @S data object Default : Registration

            @S data class ViaTelegram(
                val telegramFirstName: Optional<String>,
                val telegramLastName: Optional<String>,
                val telegramAvatar: Optional<File>,
                val telegramPhoneNumber: Optional<String>,
            ) : Registration
        }

        @S data class ConfirmCode(val source: ConfirmCodeSource, val code: String) : Step<ConfirmCodeResult>

        @S data object ConfirmPassword : Step<ConfirmPasswordResult>

        /**
         * TelegramOpenIDConnect
         */
        @S data class TelegramOIDC(
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
        ) : Step<TelegramOIDCResult>
    }

    @S sealed interface ConfirmCodeSource {
        @S data class PhoneNumber(val phoneNumber: String) : ConfirmCodeSource
        @S data class Email(val email: String) : ConfirmCodeSource
    }

    @S enum class RegistrationResult : StepResult { Accepted, Rejected }

    @S enum class ConfirmCodeResult : StepResult { Valid, Invalid }

    @S enum class ConfirmPasswordResult : StepResult { Valid, Invalid }

    @S enum class TelegramOIDCResult : StepResult { Valid, Invalid }
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
    confirmPassword: (Step.ConfirmPassword, ConfirmPasswordResult) -> R,
    telegramOIDC: (Step.TelegramOIDC, TelegramOIDCResult) -> R,
): R = when (step) {
    is Step.Registration -> registration(step, stepResult as RegistrationResult)
    is Step.ConfirmCode -> confirmCode(step, stepResult as ConfirmCodeResult)
    is Step.ConfirmPassword -> confirmPassword(step, stepResult as ConfirmPasswordResult)
    is Step.TelegramOIDC ->telegramOIDC(step, stepResult as TelegramOIDCResult)
}

inline fun Step<*>.finalize(
    registration: (Step.Registration) -> RegistrationResult,
    confirmCode: (Step.ConfirmCode) -> ConfirmCodeResult,
    confirmPassword: (Step.ConfirmPassword) -> ConfirmPasswordResult,
    telegramOIDC: (Step.TelegramOIDC) -> TelegramOIDCResult,
): PreviousStep = when (this) {
    is Step.Registration -> finalize(registration(this))
    is Step.ConfirmCode -> finalize(confirmCode(this))
    is Step.ConfirmPassword -> finalize(confirmPassword(this))
    is Step.TelegramOIDC -> finalize(telegramOIDC(this))
}

fun Step.Registration.finalize(result: RegistrationResult): PreviousStep = PreviousStep(this, result)
fun Step.ConfirmCode.finalize(result: ConfirmCodeResult): PreviousStep = PreviousStep(this, result)
fun Step.ConfirmPassword.finalize(result: ConfirmPasswordResult): PreviousStep = PreviousStep(this, result)
fun Step.TelegramOIDC.finalize(result: TelegramOIDCResult): PreviousStep = PreviousStep(this, result)
