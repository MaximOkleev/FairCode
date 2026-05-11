package com.team.antiplagiat.service

import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.config.TokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {
    fun authenticate(login: String, password: String): String {
        logger.debug { "Попытка аутентификации пользователя: $login" }
        val user = userRepository.findByLogin(login)
            ?: throw IllegalArgumentException("Invalid credentials").also {
                logger.warn { "Пользователь не найден: $login" }
            }

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            logger.warn { "Неверный пароль для пользователя: $login" }
            throw IllegalArgumentException("Invalid credentials")
        }

        if (!user.emailVerified) {
            logger.warn { "Попытка входа с неподтвёрженным email: ${user.email}" }
            throw EmailNotVerifiedException("Email не подтвёржен. Письмо с ссылкой верификации отправлено на ${user.email}", user.email)
        }

        logger.info { "Пользователь успешно аутентифицирован: $login" }
        return tokenService.generateToken(user)
    }
}

class EmailNotVerifiedException(message: String, val email: String) : Exception(message)
