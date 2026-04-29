package domain.service

import backend.core.types.ConfirmCodeDestination
import backend.core.types.FileId
import backend.core.types.LinkEmailInfo
import backend.core.types.LinkPhoneNumberInfo
import backend.core.types.LoginCapabilities
import backend.core.types.LoginMethod
import backend.core.types.LoginState
import backend.core.types.OAuthSessionInfo
import backend.core.types.PreFilledRegistrationField
import backend.core.types.PreFilledRegistrationFieldSource
import backend.core.types.PreFilledRegistrationFields
import backend.core.types.SessionId
import domain.event.LoginStateChanged
import domain.service.InternalLoginState.ConfirmCodeResult
import domain.service.InternalLoginState.InitiatedWith
import domain.service.InternalLoginState.Step
import domain.service.InternalLoginState.TelegramOAuthResult
import domain.service.result.LogInError
import domain.service.result.RegisterUserError
import integration.eventCollector.EventCollector
import integration.loginRepository.LoginRepository
import integration.telegramOpenidConnect.TelegramOpenidConnect
import integration.telegramOpenidConnect.ValidateTelegramOpenidConnectResult
import io.github.oshai.kotlinlogging.KotlinLogging
import y9to.common.types.Birthday
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import y9to.libs.stdlib.optional.getOrElse
import y9to.libs.stdlib.optional.map
import y9to.libs.stdlib.optional.none
import y9to.libs.stdlib.successOrElse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


val logger = KotlinLogging.logger { }

class LoginServiceImpl(
    private val authService: AuthService,
    private val userService: UserService,
    private val eventCollector: EventCollector,
    private val loginRepository: LoginRepository,
    private val telegramOpenidConnect: TelegramOpenidConnect?,
    private val loginStepTTL: Duration, // = 5.minutes
    private val confirmCodeLength: () -> Int, // = { listOf(4, 6).random() }
    private val redirectUri: (SessionId) -> String,
    private val requiredToLinkPhoneNumberWhileTelegramOAuthRegistration: Boolean,
    private val debugLoginCodes: Set<String>,
) : LoginService {
    override suspend fun getLoginCapabilities(session: SessionId): LoginCapabilities {
        return LoginCapabilities(
            availableLoginMethods = buildSet {
                add(LoginMethod.Email)
                add(LoginMethod.Phone)
                if (telegramOpenidConnect != null)
                    add(LoginMethod.TelegramOAuth)
            },
            requiredToLinkPhoneNumberWhileTelegramOAuthRegistration = requiredToLinkPhoneNumberWhileTelegramOAuthRegistration,
        )
    }

    override suspend fun getLoginState(session: SessionId): LoginState? {
        val internalState = loginRepository.getLoginState(session)
            ?: return null

        return when (val step = internalState.currentStep) {
            is Step.ConfirmCode -> LoginState.ConfirmCode(
                digitsOnly = step.code.all { it.isDigit() },
                length = step.code.length,
                destination = step.destination,
            )

            is Step.Password2FA -> LoginState.Password2FA(
                hint = null,
            )

            is Step.Registration.Default -> LoginState.Registration(
                preFilledRegistrationFields = PreFilledRegistrationFields(),
                linkPhoneNumberInfo = when (val initiatedWith = internalState.initiatedWith) {
                    is InitiatedWith.PhoneNumber -> LinkPhoneNumberInfo.Mandatory(initiatedWith.phoneNumber)
                    is InitiatedWith.TelegramOAuth,
                    is InitiatedWith.Email -> LinkPhoneNumberInfo.None
                },
                linkEmailInfo = when (val initiatedWith = internalState.initiatedWith) {
                    is InitiatedWith.Email -> LinkEmailInfo.Mandatory(initiatedWith.email)
                    is InitiatedWith.TelegramOAuth,
                    is InitiatedWith.PhoneNumber -> LinkEmailInfo.None
                },
            )

            is Step.Registration.UsingTelegramOAuthProfileInfo -> {
                val preFilledRegistrationFields = PreFilledRegistrationFields(
                    firstName = step.telegramFirstName
                        .map { PreFilledRegistrationField(it, PreFilledRegistrationFieldSource.Telegram) }
                        .getOrNull(),
                    lastName = step.telegramLastName
                        .map { PreFilledRegistrationField(it, PreFilledRegistrationFieldSource.Telegram) }
                        .getOrNull(),
                    avatar = step.telegramAvatar
                        .map { PreFilledRegistrationField(it, PreFilledRegistrationFieldSource.Telegram) }
                        .getOrNull(),
                )

                val linkPhoneNumberInfo =
                    if (step.telegramPhoneNumber.isPresent) {
                        val canUseTelegramPhoneNumber = run {
                            val user = userService.findByPhoneNumber(step.telegramPhoneNumber.getOrThrow())
                            val telegramPhoneNumberAvailable = user == null
                            telegramPhoneNumberAvailable
                        }

                        if (canUseTelegramPhoneNumber) {
                            if (requiredToLinkPhoneNumberWhileTelegramOAuthRegistration) {
                                LinkPhoneNumberInfo.Mandatory(step.telegramPhoneNumber.getOrThrow())
                            } else {
                                LinkPhoneNumberInfo.Optional(step.telegramPhoneNumber.getOrThrow())
                            }
                        } else {
                            LinkPhoneNumberInfo.Restricted(
                                step.telegramPhoneNumber.getOrThrow(),
                                LinkPhoneNumberInfo.RestrictedReason.OtherUserThisThisPhoneNumberExists,
                            )
                        }
                    } else {
                        LinkPhoneNumberInfo.None
                    }

                val linkEmailInfo = LinkEmailInfo.None

                LoginState.Registration(
                    preFilledRegistrationFields = preFilledRegistrationFields,
                    linkPhoneNumberInfo = linkPhoneNumberInfo,
                    linkEmailInfo = linkEmailInfo,
                )
            }

            is Step.TelegramOAuth -> LoginState.OAuthInProgress(
                sessionInfo = OAuthSessionInfo.Telegram(
                    isPhoneNumberWasRequested = step.isPhoneNumberWasRequested,
                ),
                authorizationUri = step.authorizationUri,
            )
        }
    }

    override suspend fun startWithPhoneNumber(session: SessionId, phoneNumber: String): StartWithPhoneNumberResult {
        loginRepository.saveLoginState(
            session = session,
            state = InternalLoginState(
                initiatedWith = InitiatedWith.PhoneNumber(phoneNumber),
                previousSteps = emptyList(),
                currentStep = Step.ConfirmCode(
                    destination = ConfirmCodeDestination.Phone(phoneNumber),
                    code = generateConfirmCode(confirmCodeLength()),
                )
            ),
            ttl = 5.minutes,
        )

        eventCollector.emit(LoginStateChanged(session, getLoginState(session)))

        return Unit.asOk()
    }

    override suspend fun startWithEmail(session: SessionId, email: String): StartWithEmailResult {
        loginRepository.saveLoginState(
            session = session,
            state = InternalLoginState(
                initiatedWith = InitiatedWith.Email(email),
                previousSteps = emptyList(),
                currentStep = Step.ConfirmCode(
                    destination = ConfirmCodeDestination.Email(email),
                    code = generateConfirmCode(confirmCodeLength()),
                )
            ),
            ttl = loginStepTTL,
        )

        eventCollector.emit(LoginStateChanged(session, getLoginState(session)))

        return Unit.asOk()
    }

    override suspend fun startWithTelegramOAuth(session: SessionId, requestPhoneNumber: Boolean): StartWithTelegramOAuthResult {
        if (telegramOpenidConnect == null)
            return StartLoginError.UnavailableLoginMethod.asError()

        val result = telegramOpenidConnect.initiate(
            redirectUri = redirectUri(session),
            requestProfile = true,
            requestPhoneNumber = requestPhoneNumber,
            requestBotAccess = false,
        )

        val newState = InternalLoginState(
            initiatedWith = InitiatedWith.TelegramOAuth(authorizationId = null),
            previousSteps = emptyList(),
            currentStep = Step.TelegramOAuth(
                isPhoneNumberWasRequested = requestPhoneNumber,
                authorizationUri = result.authorizationUri,
                authorizationState = result.authorizationState,
                authorizationCodeVerifier = result.codeVerifier,
            ),
        )

        loginRepository.saveLoginState(session, newState, loginStepTTL)
        eventCollector.emit(LoginStateChanged(session, getLoginState(session)))

        return Unit.asOk()
    }


    override suspend fun checkConfirmCode(session: SessionId, code: String): CheckConfirmCodeResult {
        val internalState = loginRepository.getLoginState(session)
        val inStep = internalState?.currentStep as? Step.ConfirmCode
            ?: return ContinueLoginError.Unexpected.asError()

        if (code != inStep.code && code !in debugLoginCodes) {
            val newState = internalState.copy(
                previousStep = inStep.finalize(ConfirmCodeResult.Invalid),
                nextStep = inStep,
            )

            loginRepository.saveLoginState(session, newState, loginStepTTL)
            eventCollector.emit(LoginStateChanged(session, getLoginState(session)))

            return CheckConfirmCodeError.InvalidConfirmCode.asError()
        }

        val user = when (val initiatedWith = internalState.initiatedWith) {
            is InitiatedWith.Email -> userService.findByPhoneNumber(initiatedWith.email)
            is InitiatedWith.PhoneNumber -> userService.findByPhoneNumber(initiatedWith.phoneNumber)
            is InitiatedWith.TelegramOAuth -> {
                if (internalState.confirmedTelegramAuthorizationId == null) {
                    val message = "Login flow error. Confirm code cannot be prompted BEFORE TelegramOAuth phase is passed"
                    logger.error { message }
                    loginRepository.resetLoginState(session)
                    eventCollector.emit(LoginStateChanged(session, null))
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

                state = when (val source = inStep.destination) {
                    is ConfirmCodeDestination.Phone -> state.copy(confirmedPhoneNumber = source.phoneNumber)
                    is ConfirmCodeDestination.Email -> state.copy(confirmedEmail = source.email)
                }

                state
            }

            loginRepository.saveLoginState(session, newState, loginStepTTL)
            eventCollector.emit(LoginStateChanged(session, getLoginState(session)))

            return Unit.asOk()
        }

        loginRepository.resetLoginState(session)
        eventCollector.emit(LoginStateChanged(session, null))
        authService.logIn(session, user.id)
            .successOrElse { error ->
                return when (error) {
                    LogInError.AlreadyAuthenticated -> ContinueLoginError.LoginAttemptRejected
                    LogInError.InvalidClientId -> ContinueLoginError.LoginAttemptRejected
                    LogInError.InvalidSessionId -> ContinueLoginError.InvalidSessionId
                }.asError()
            }

        return Unit.asOk()
    }

    override suspend fun checkPassword2FA(session: SessionId, password: String): CheckPassword2FAResult {
        return ContinueLoginError.Unexpected.asError()
    }

    override suspend fun checkOAuth(
        session: SessionId,
        authorizationCode: String,
        authorizationState: String
    ): CheckOAuthResult {
        val internalState = loginRepository.getLoginState(session)
        val inStep = internalState?.currentStep as? Step.TelegramOAuth
            ?: return ContinueLoginError.Unexpected.asError()

        if (telegramOpenidConnect == null) {
            loginRepository.resetLoginState(session)
            eventCollector.emit(LoginStateChanged(session, null))
            return ContinueLoginError.LoginAttemptRejected.asError()
        }

        val result = telegramOpenidConnect.validate(
            redirectUri = redirectUri(session),
            authorizationCode = authorizationCode,
            codeVerifier = inStep.authorizationCodeVerifier
        )

        val profile = when (result) {
            is ValidateTelegramOpenidConnectResult.Ok -> result.profile

            ValidateTelegramOpenidConnectResult.InvalidAuthorizationCode -> {
                loginRepository.resetLoginState(session)
                eventCollector.emit(LoginStateChanged(session, null))
                return CheckOAuthError.InvalidAuthorizationCode.asError()
            }
        }

        val user = when (val initiatedWith = internalState.initiatedWith) {
            is InitiatedWith.Email -> userService.findByPhoneNumber(initiatedWith.email)
            is InitiatedWith.PhoneNumber -> userService.findByPhoneNumber(initiatedWith.phoneNumber)
            is InitiatedWith.TelegramOAuth -> userService.findByTelegramAuthId(profile.authorizationId)
        }

        if (user == null) {
            val newState = internalState
                .copy(initiatedWith = (internalState.initiatedWith as InitiatedWith.TelegramOAuth).copy(
                    authorizationId = profile.authorizationId
                ))
                .copy(confirmedTelegramAuthorizationId = profile.authorizationId)
                .copy(
                    previousStep = inStep.finalize(TelegramOAuthResult.Valid),
                    nextStep = Step.Registration.UsingTelegramOAuthProfileInfo(
                        telegramFirstName = profile.name,
                        telegramLastName = none(),
                        telegramAvatar = none(), // todo
                        telegramPhoneNumber = profile.phoneNumber,
                    ),
                )

            loginRepository.saveLoginState(session, newState, loginStepTTL)
            eventCollector.emit(LoginStateChanged(session, getLoginState(session)))

            return Unit.asOk()
        }

        loginRepository.resetLoginState(session)
        eventCollector.emit(LoginStateChanged(session, null))
        authService.logIn(session, user.id)
            .successOrElse { error ->
                return when (error) {
                    LogInError.AlreadyAuthenticated -> ContinueLoginError.LoginAttemptRejected
                    LogInError.InvalidClientId -> ContinueLoginError.LoginAttemptRejected
                    LogInError.InvalidSessionId -> ContinueLoginError.InvalidSessionId
                }.asError()
            }

        return Unit.asOk()
    }

    override suspend fun register(
        session: SessionId,
        firstName: String,
        lastName: String?,
        bio: String?,
        birthday: Birthday?,
        avatar: FileId?,
        cover: FileId?,
        linkPhoneNumber: Boolean,
        linkEmail: Boolean
    ): RegisterResult {
        val internalState = loginRepository.getLoginState(session)
        val inStep = internalState?.currentStep as? Step.Registration
            ?: return ContinueLoginError.Unexpected.asError()

        // Checks

        val isTelegramOAuth = internalState.initiatedWith is InitiatedWith.TelegramOAuth
        val linkPhoneNumberRequired = isTelegramOAuth && requiredToLinkPhoneNumberWhileTelegramOAuthRegistration

        if (linkPhoneNumberRequired && !linkPhoneNumber) {
            return RegisterError.PhoneNumberLinkageRequired.asError()
        }

        val telegramAuthId = if (isTelegramOAuth) {
            (internalState.initiatedWith as InitiatedWith.TelegramOAuth).authorizationId
        } else {
            null
        }

        val phoneNumber = if (linkPhoneNumber) {
            when (inStep) {
                is Step.Registration.Default -> if (internalState.confirmedPhoneNumber != null) {
                    internalState.confirmedPhoneNumber
                } else {
                    return RegisterError.CannotLinkPhoneNumber.asError()
                }

                is Step.Registration.UsingTelegramOAuthProfileInfo -> {
                    inStep.telegramPhoneNumber
                        .getOrElse { return RegisterError.CannotLinkPhoneNumber.asError() }
                }
            }
        } else {
            null
        }

        val email = if (linkEmail) {
            when (inStep) {
                is Step.Registration.Default -> if (internalState.confirmedEmail != null) {
                    internalState.confirmedEmail
                } else {
                    return RegisterError.CannotLinkEmail.asError()
                }

                is Step.Registration.UsingTelegramOAuthProfileInfo -> {
                    return RegisterError.CannotLinkEmail.asError()
                }
            }
        } else {
            null
        }

        // Registration

        val user = userService.register(
            session = session,
            firstName = firstName,
            lastName = lastName,
            telegramAuthId = telegramAuthId,
            email = email,
            phoneNumber = phoneNumber,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        ).successOrElse { error ->
            return when (error) {
                RegisterUserError.PhoneNumberConflict -> RegisterError.CannotLinkPhoneNumber
                RegisterUserError.EmailConflict -> RegisterError.CannotLinkEmail
            }.asError()
        }

        loginRepository.resetLoginState(session)
        eventCollector.emit(LoginStateChanged(session, null))
        authService.logIn(session, user.id)
            .successOrElse { error ->
                return when (error) {
                    LogInError.AlreadyAuthenticated,
                    LogInError.InvalidSessionId -> ContinueLoginError.InvalidSessionId
                    LogInError.InvalidClientId -> {
                        val message = "Client with id ${user.id} not found but it is registered right now!"
                        logger.error { message }
                        error(message)
                    }
                }.asError()
            }

        return Unit.asOk()
    }

    override suspend fun cancelLogin(session: SessionId) {
        loginRepository.resetLoginState(session)
        eventCollector.emit(LoginStateChanged(session, null))
    }
}

private fun generateConfirmCode(length: Int): String {
    require(length >= 0) { "length cannot be negative" }
    return (1..length).map { "0123456789".random() }.joinToString("")
}
