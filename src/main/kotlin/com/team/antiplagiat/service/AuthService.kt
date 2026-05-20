package com.team.antiplagiat.service

import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.config.TokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import com.team.antiplagiat.exception.InvalidCredentialsException
import com.team.antiplagiat.exception.ResourceNotFoundException
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
    private val securityAlertService: SecurityAlertService,
    private val meterRegistry: MeterRegistry
) {
    fun authenticate(login: String, password: String): String {
        logger.debug { "Authentication attempt" }
        val user = userRepository.findByLogin(login)
            ?: userRepository.findByEmail(login)
            ?: run {
                logger.warn { "Authentication failed: user not found" }
                meterRegistry.counter("auth.login.failed.not_found").increment()
                meterRegistry.counter("auth.login.failed.total").increment()
                throw InvalidCredentialsException()
            }

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            logger.warn { "Authentication failed: invalid password" }
            meterRegistry.counter("auth.login.failed.invalid_password").increment()
            meterRegistry.counter("auth.login.failed.total").increment()
            throw InvalidCredentialsException()
        }

        if (!user.emailVerified) {
            logger.warn { "Authentication blocked: email is not verified, userId=${user.id}" }
            meterRegistry.counter("auth.login.failed.email_not_verified").increment()
            meterRegistry.counter("auth.login.failed.total").increment()
            throw EmailNotVerifiedException(
                "Email не подтверждён. Письмо с ссылкой верификации отправлено повторно.",
                user.email
            )
        }

        logger.info { "Authentication successful: userId=${user.id}" }
        meterRegistry.counter("auth.login.success").increment()
        try {
            securityAlertService.sendLoginAlert(user)
            meterRegistry.counter("auth.login.security_alert.sent").increment()
        } catch (e: Exception) {
            logger.error(e) { "Не удалось отправить security alert для пользователя ${user.id}" }
            meterRegistry.counter("auth.login.security_alert.failed").increment()
        }
        return tokenService.generateToken(user)
    }

    @Transactional
    fun changePassword(userId: Long, oldPassword: String, newPassword: String) {
        logger.info { "Попытка смены пароля для пользователя $userId" }
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Пользователь с id=$userId не найден") }

        if (!passwordEncoder.matches(oldPassword, user.passwordHash)) {
            logger.warn { "Неверный текущий пароль при смене пароля для пользователя $userId" }
            meterRegistry.counter("auth.password_change.failed.invalid_password").increment()
            throw InvalidCredentialsException()
        }

        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)
        meterRegistry.counter("auth.password_change.success").increment()
        logger.info { "Пароль успешно изменён для пользователя $userId" }
    }
}

class EmailNotVerifiedException(message: String, val email: String) : Exception(message)
