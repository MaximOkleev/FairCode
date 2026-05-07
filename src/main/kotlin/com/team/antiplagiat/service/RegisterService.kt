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
    private val tokenService: com.team.antiplagiat.config.TokenService
) {

    private val registrationCounter: Counter = Counter.builder("registration.success")
        .description("Количество успешных регистраций")
        .register(meterRegistry)

    private val registrationFailureCounter: Counter = Counter.builder("registration.failure")
        .description("Количество неудачных попыток регистрации")
        .register(meterRegistry)

    @Transactional
    fun register(request: RegisterRequest): RegisterResponse {
        logger.info { "Попытка регистрации: email=${request.email}" }
        logger.debug { "Проверка уникальности email=${request.email}" }

        if (userRepository.findByEmail(request.email) != null) {
            logger.warn { "Регистрация отклонена: email '${request.email}' уже используется" }
            registrationFailureCounter.increment()
            throw IllegalArgumentException("Email '${request.email}' уже зарегистрирован")
        }

        val hashedPassword = passwordEncoder.encode(request.password)
        logger.debug { "Пароль хеширован для email=${request.email}" }

        val login = request.email.substringBefore("@")
        val user = User(
            login = login,
            email = request.email,
            passwordHash = hashedPassword,
            role = User.Role.BASIC
        )

        val saved = userRepository.save(user)
        logger.debug { "Пользователь сохранён: id=${saved.id}, email=${saved.email}" }
        logger.info { "Регистрация успешна: id=${saved.id}, email=${saved.email}" }

        registrationCounter.increment()

        val token = tokenService.generateToken(saved)
        return RegisterResponse(userId = saved.id, email = saved.email, token = token)
    }
}