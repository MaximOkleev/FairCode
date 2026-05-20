package com.team.antiplagiat.service

import com.team.antiplagiat.controller.dto.register.RegisterRequest
import com.team.antiplagiat.controller.dto.register.RegisterResponse
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class RegisterService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    meterRegistry: MeterRegistry,
    private val emailVerificationService: EmailVerificationService
) {

    private val registrationCounter: Counter = Counter.builder("registration.success")
        .description("Количество успешных регистраций")
        .register(meterRegistry)

    private val registrationFailureCounter: Counter = Counter.builder("registration.failure")
        .description("Количество неудачных попыток регистрации")
        .register(meterRegistry)

    @Transactional
    fun register(request: RegisterRequest): RegisterResponse {
        logger.info { "Registration attempt" }
        logger.debug { "Checking email uniqueness" }

        if (userRepository.findByEmail(request.email) != null) {
            logger.warn { "Registration rejected: email already exists" }
            registrationFailureCounter.increment()
            throw IllegalArgumentException("Email already registered")
        }

        val hashedPassword = passwordEncoder.encode(request.password)
        logger.debug { "Password encoded" }

        val login = request.email.substringBefore("@")
        val user = User(
            login = login,
            email = request.email,
            passwordHash = hashedPassword,
            role = User.Role.BASIC
        )

        val saved = userRepository.save(user)
        logger.debug { "User saved: userId=${saved.id}" }
        logger.info { "Registration successful: userId=${saved.id}" }

        registrationCounter.increment()
        logger.info { "Verification email sent: userId=${saved.id}" }
        emailVerificationService.sendVerification(saved.email)

        return RegisterResponse(
            userId = saved.id,
            login = saved.login,
            email = saved.email,
            message = "Пользователь зарегистрирован. Проверьте почту для подтверждения email",
            emailVerificationRequired = true
        )
    }
}