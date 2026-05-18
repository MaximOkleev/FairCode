package com.team.antiplagiat.service

import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.config.TokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
    private val meterRegistry: MeterRegistry
) {
    fun authenticate(login: String, password: String): String {
        logger.debug { "Попытка аутентификации пользователя: $login" }
        val user = userRepository.findByLogin(login)
            ?: run {
                logger.warn { "Пользователь не найден: $login" }
                meterRegistry.counter("auth.login.failed.not_found").increment()
                meterRegistry.counter("auth.login.failed.total").increment()
                throw IllegalArgumentException("Invalid credentials")
            }

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            logger.warn { "Неверный пароль для пользователя: $login" }
            meterRegistry.counter("auth.login.failed.invalid_password").increment()
            meterRegistry.counter("auth.login.failed.total").increment()
            throw IllegalArgumentException("Invalid credentials")
        }

        if (!user.emailVerified) {
            logger.warn { "Попытка входа с неподтвёрженным email: ${user.email}" }
            meterRegistry.counter("auth.login.failed.email_not_verified").increment()
            meterRegistry.counter("auth.login.failed.total").increment()
            throw EmailNotVerifiedException(
                "Email не подтвёржен. Письмо с ссылкой верификации отправлено на ${user.email}",
                user.email
            )
        }

        logger.info { "Пользователь успешно аутентифицирован: $login" }
        meterRegistry.counter("auth.login.success").increment()
        return tokenService.generateToken(user)
    }
}

class EmailNotVerifiedException(message: String, val email: String) : Exception(message)
