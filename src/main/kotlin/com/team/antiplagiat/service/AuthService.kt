package com.team.antiplagiat.service

import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.config.TokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import com.team.antiplagiat.exception.InvalidCredentialsException

private val logger = KotlinLogging.logger {}

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
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
        return tokenService.generateToken(user)
    }
}

class EmailNotVerifiedException(message: String, val email: String) : Exception(message)
