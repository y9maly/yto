package domain.service

import backend.core.types.FileId
import backend.core.types.LoginState
import backend.core.types.SessionId
import domain.service.InternalLoginState.ConfirmCodeResult
import domain.service.InternalLoginState.ConfirmCodeSource
import domain.service.InternalLoginState.InitiatedWith
import domain.service.InternalLoginState.Step
import domain.service.InternalLoginState.TelegramOIDCResult
import domain.service.result.LogInError
import integration.loginRepository.LoginRepository
import integration.telegramOpenidConnect.TelegramOpenidConnect
import integration.telegramOpenidConnect.ValidateTelegramOpenidConnectResult
import io.github.oshai.kotlinlogging.KotlinLogging
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.optional.fold
import y9to.libs.stdlib.optional.getOrElse
import y9to.libs.stdlib.optional.none
import y9to.libs.stdlib.optional.presentIfNotNull
import y9to.libs.stdlib.successOrElse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


val logger = KotlinLogging.logger { }

class LoginServiceImpl(
    private val authService: AuthService,
    private val userService: UserService,
    private val loginRepository: LoginRepository,
    private val telegramOpenidConnect: TelegramOpenidConnect,
    private val loginStepTTL: Duration, // = 5.minutes
    private val confirmCodeLength: () -> Int, // = { listOf(4, 6).random() }
    private val redirectUri: (SessionId) -> String,
) : LoginService {
    override suspend fun getLoginState(session: SessionId): LoginState {
        val internalState = loginRepository.getLoginState(session)
            ?: return LoginState.None

        return when (val step = internalState.currentStep) {
            is Step.ConfirmCode -> LoginState.WaitConfirmCode(
                digitOnly = step.code.all { it.isDigit() },
                length = step.code.length,
            )

            is Step.ConfirmPassword -> LoginState.WaitPassword(
                hint = null,
            )

            is Step.Registration.Default -> LoginState.WaitRegistration.WaitRegistrationDefault

            is Step.Registration.ViaTelegram -> {
                val canUseTelegramPhoneNumber = step.telegramPhoneNumber.fold(
                    onNone = { false },
                    onPresent = { telegramPhoneNumber ->
                        val user = userService.findByPhoneNumber(telegramPhoneNumber)
                        val telegramPhoneNumberAvailable = user == null
                        telegramPhoneNumberAvailable
                    }
                )

                LoginState.WaitRegistration.WaitRegistrationViaTelegram(
                    telegramFirstName = step.telegramFirstName,
                    telegramLastName = step.telegramLastName,
                    telegramAvatar = step.telegramAvatar,
                    telegramPhoneNumber = step.telegramPhoneNumber,
                    canUseTelegramPhoneNumber = canUseTelegramPhoneNumber,
                )
            }

            is Step.TelegramOIDC -> LoginState.TelegramOIDC(
                authorizationUri = step.authorizationUri,
            )
        }
    }

    override suspend fun startWithPhoneNumber(session: SessionId, phoneNumber: String): StartWithPhoneNumberResult {
        val user = userService.findByPhoneNumber(phoneNumber)
            ?: return StartWithPhoneNumberError.InvalidPhoneNumber.asError()

        loginRepository.saveLoginState(
            session = session,
            state = InternalLoginState(
                initiatedWith = InitiatedWith.PhoneNumber(phoneNumber),
                previousSteps = emptyList(),
                currentStep = Step.ConfirmCode(
                    source = ConfirmCodeSource.PhoneNumber(phoneNumber),
                    code = generateConfirmCode(confirmCodeLength()),
                )
            ),
            ttl = 5.minutes,
        )

        return Unit.asOk()
    }

    override suspend fun startWithEmail(session: SessionId, email: String): StartWithEmailResult {
        val user = userService.findByEmail(email)
            ?: return StartWithEmailError.InvalidEmail.asError()

        loginRepository.saveLoginState(
            session = session,
            state = InternalLoginState(
                initiatedWith = InitiatedWith.Email(email),
                previousSteps = emptyList(),
                currentStep = Step.ConfirmCode(
                    source = ConfirmCodeSource.Email(email),
                    code = generateConfirmCode(confirmCodeLength()),
                )
            ),
            ttl = loginStepTTL,
        )

        return Unit.asOk()
    }

    override suspend fun startWithTelegramOIDC(session: SessionId, requestPhoneNumber: Boolean): StartWithTelegramOIDCResult {
        val result = telegramOpenidConnect.initiate(
            redirectUri = redirectUri(session),
            requestProfile = true,
            requestPhoneNumber = requestPhoneNumber,
            requestBotAccess = false,
        )

        val newState = InternalLoginState(
            initiatedWith = InitiatedWith.TelegramOIDC,
            previousSteps = emptyList(),
            currentStep = Step.TelegramOIDC(
                authorizationUri = result.authorizationUri,
                authorizationState = result.authorizationState,
                authorizationCodeVerifier = result.codeVerifier,
            ),
        )

        loginRepository.saveLoginState(session, newState, loginStepTTL)

        return StartWithTelegramOIDCOk(uri = result.authorizationUri).asOk()
    }

    override suspend fun checkConfirmCode(session: SessionId, code: String): CheckConfirmCodeResult {
        val internalState = loginRepository.getLoginState(session)
        val inStep = internalState?.currentStep as? Step.ConfirmCode
            ?: return CheckConfirmCodeError.Unexpected.asError()

        if (code != inStep.code) {
            val newState = internalState.copy(
                previousStep = inStep.finalize(ConfirmCodeResult.Invalid),
                nextStep = inStep,
            )

            loginRepository.saveLoginState(session, newState, loginStepTTL)

            return CheckConfirmCodeError.InvalidConfirmCode.asError()
        }

        val user = when (val initiatedWith = internalState.initiatedWith) {
            is InitiatedWith.Email -> userService.findByPhoneNumber(initiatedWith.email)
            is InitiatedWith.PhoneNumber -> userService.findByPhoneNumber(initiatedWith.phoneNumber)
            is InitiatedWith.TelegramOIDC -> {
                if (internalState.confirmedTelegramAuthorizationId == null) {
                    val message = "Login flow error. Confirm code cannot be prompted BEFORE TelegramOIDC phase is passed"
                    logger.error { message }
                    loginRepository.resetLoginState(session)
                    error(message)
                }

                userService.findByTelegramAuthId(internalState.confirmedTelegramAuthorizationId!!)
            }
        }

        if (user == null) {
            val newState = run {
                var state = internalState
                    .copy(
                        previousStep = inStep.finalize(ConfirmCodeResult.Valid),
                        nextStep = Step.Registration.Default,
                    )

                state = when (val source = inStep.source) {
                    is ConfirmCodeSource.PhoneNumber -> state.copy(confirmedPhoneNumber = source.phoneNumber)
                    is ConfirmCodeSource.Email -> state.copy(confirmedEmail = source.email)
                }

                state
            }

            loginRepository.saveLoginState(session, newState, loginStepTTL)

            return Unit.asOk()
        }

        loginRepository.resetLoginState(session)
        authService.logIn(session, user.id)
            .successOrElse { error ->
                return when (error) {
                    LogInError.AlreadyLogInned -> CheckConfirmCodeError.LoginAttemptRejected
                    LogInError.InvalidSessionId -> CheckConfirmCodeError.InvalidSessionId
                    LogInError.InvalidClientId -> CheckConfirmCodeError.LoginAttemptRejected
                }.asError()
            }

        return Unit.asOk()
    }

    override suspend fun checkConfirmPassword(session: SessionId, password: String): CheckConfirmPasswordResult {
        return CheckConfirmPasswordError.Unexpected.asError()
    }

    override suspend fun checkTelegramOIDC(
        session: SessionId,
        authorizationCode: String,
        state: String
    ): CheckTelegramOIDCResult {
        val internalState = loginRepository.getLoginState(session)
        val inStep = internalState?.currentStep as? Step.TelegramOIDC
            ?: return CheckTelegramOIDCError.Unexpected.asError()

        val result = telegramOpenidConnect.validate(
            redirectUri = redirectUri(session),
            authorizationCode = authorizationCode,
            codeVerifier = inStep.authorizationCodeVerifier
        )

        val profile = when (result) {
            is ValidateTelegramOpenidConnectResult.Ok -> result.profile

            ValidateTelegramOpenidConnectResult.InvalidAuthorizationCode -> {
                loginRepository.resetLoginState(session)
                return CheckTelegramOIDCError.InvalidAuthorizationCode.asError()
            }
        }

        val user = when (val initiatedWith = internalState.initiatedWith) {
            is InitiatedWith.Email -> userService.findByPhoneNumber(initiatedWith.email)
            is InitiatedWith.PhoneNumber -> userService.findByPhoneNumber(initiatedWith.phoneNumber)
            is InitiatedWith.TelegramOIDC -> userService.findByTelegramAuthId(profile.authorizationId)
        }

        if (user == null) {
            val newState = internalState
                .copy(confirmedTelegramAuthorizationId = profile.authorizationId)
                .copy(
                    previousStep = inStep.finalize(TelegramOIDCResult.Valid),
                    nextStep = Step.Registration.ViaTelegram(
                        telegramFirstName = profile.name,
                        telegramLastName = none(),
                        telegramAvatar = none(), // todo
                        telegramPhoneNumber = profile.phoneNumber,
                    ),
                )

            loginRepository.saveLoginState(session, newState, loginStepTTL)

            return Unit.asOk()
        }

        loginRepository.resetLoginState(session)
        authService.logIn(session, user.id)
            .successOrElse { error ->
                return when (error) {
                    LogInError.AlreadyLogInned -> CheckTelegramOIDCError.LoginAttemptRejected
                    LogInError.InvalidSessionId -> CheckTelegramOIDCError.InvalidSessionId
                    LogInError.InvalidClientId -> CheckTelegramOIDCError.LoginAttemptRejected
                }.asError()
            }

        return Unit.asOk()
    }

    override suspend fun registerDefault(
        session: SessionId,
        firstName: String,
        lastName: String,
        bio: String?,
        birthday: Birthday?,
        cover: FileId?,
        avatar: FileId?,
    ): RegisterDefaultResult {
        val internalState = loginRepository.getLoginState(session)
        val inStep = internalState?.currentStep as? Step.Registration.Default
            ?: return RegisterDefaultError.Unexpected.asError()

        val (email, phoneNumber) = when (val initiatedWith = internalState.initiatedWith) {
            is InitiatedWith.PhoneNumber -> null to initiatedWith.phoneNumber

            is InitiatedWith.Email -> initiatedWith.email to null

            is InitiatedWith.TelegramOIDC -> {
                val message = "Step Step.Registration.Default must not be used when login was instantiated via TelegramOIDC"
                logger.error { message }
                error(message)
            }
        }

        val user = userService.register(
            session = session,
            firstName = firstName,
            lastName = presentIfNotNull(lastName),
            email = presentIfNotNull(email),
            phoneNumber = presentIfNotNull(phoneNumber),
            bio = presentIfNotNull(bio),
            birthday = presentIfNotNull(birthday),
            cover = presentIfNotNull(cover),
            avatar = presentIfNotNull(avatar),
        )

        loginRepository.resetLoginState(session)
        authService.logIn(session, user.id)
            .successOrElse { error ->
                return when (error) {
                    LogInError.AlreadyLogInned -> RegisterDefaultError.LoginAttemptRejected
                    LogInError.InvalidSessionId -> RegisterDefaultError.InvalidSessionId
                    LogInError.InvalidClientId -> {
                        val message = "Client with id ${user.id} not found but it is registered right now!"
                        logger.error { message }
                        error(message)
                    }
                }.asError()
            }

        return RegisterDefaultOk(user).asOk()
    }

    override suspend fun registerViaTelegram(
        session: SessionId,
        useTelegramPhoneNumber: Boolean,
        firstName: String,
        lastName: String,
        bio: String?,
        birthday: Birthday?,
        cover: FileId?,
        avatar: FileId?
    ): RegisterViaTelegramResult {
        val internalState = loginRepository.getLoginState(session)
        val inStep = internalState?.currentStep as? Step.Registration.ViaTelegram
            ?: return RegisterViaTelegramError.Unexpected.asError()

        val phoneNumber =
            if (useTelegramPhoneNumber) inStep.telegramPhoneNumber
                .getOrElse { error("Cannot use telegram phone number") }
            else null

        val user = userService.register(
            session = session,
            firstName = firstName,
            lastName = presentIfNotNull(lastName),
            email = none(),
            phoneNumber = presentIfNotNull(phoneNumber),
            bio = presentIfNotNull(bio),
            birthday = presentIfNotNull(birthday),
            cover = presentIfNotNull(cover),
            avatar = presentIfNotNull(avatar),
        )

        loginRepository.resetLoginState(session)
        authService.logIn(session, user.id)
            .successOrElse { error ->
                return when (error) {
                    LogInError.AlreadyLogInned -> RegisterViaTelegramError.LoginAttemptRejected
                    LogInError.InvalidSessionId -> RegisterViaTelegramError.InvalidSessionId
                    LogInError.InvalidClientId -> {
                        val message = "Client with id ${user.id} not found but it is registered right now!"
                        logger.error { message }
                        error(message)
                    }
                }.asError()
            }

        return RegisterViaTelegramOk(user).asOk()
    }

//    private suspend fun register(
//        session: SessionId,
//        email: String?,
//        phoneNumber: String?,
//        telegramAuthId: String?,
//        firstName: String,
//        lastName: String,
//        bio: String?,
//        birthday: Birthday?,
//        cover: FileId?,
//        avatar: FileId?
//    ): RegisterViaTelegramResult {
//
//    }
}

private fun generateConfirmCode(length: Int): String {
    require(length >= 0) { "length cannot be negative" }
    return (1..length).map { "0123456789".random() }.joinToString("")
}
