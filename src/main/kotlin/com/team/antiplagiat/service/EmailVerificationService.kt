package com.team.antiplagiat.service

import com.team.antiplagiat.config.ResendProperties
import com.team.antiplagiat.config.TokenService
import com.team.antiplagiat.models.EmailVerificationToken
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.EmailVerificationTokenRepository
import com.team.antiplagiat.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Service
class EmailVerificationService(
    private val userRepository: UserRepository,
    private val tokenRepository: EmailVerificationTokenRepository,
    private val resendService: ResendService,
    private val properties: ResendProperties,
    private val tokenService: TokenService
) {

    companion object {
        private const val TOKEN_VALIDITY_MINUTES = 15L
        private const val RATE_LIMIT_MINUTES = 15L
    }

    @Transactional
    fun sendVerification(email: String) {
        logger.info { "Отправка письма верификации на $email" }
        val user = userRepository.findByEmail(email)
            ?: run {
                logger.warn { "Пользователь с email $email не найден" }
                return
            }

        if (user.emailVerified) {
            logger.info { "Email уже подтверждён для пользователя ${user.id}" }
            return
        }

        val lastToken = tokenRepository.findLatestByUserId(user.id)
        if (lastToken != null && lastToken.usedAt == null) {
            val minutesSinceCreation = ChronoUnit.MINUTES.between(lastToken.createdAt, Instant.now())
            if (minutesSinceCreation < RATE_LIMIT_MINUTES) {
                logger.warn { "Слишком частая попытка отправки письма для ${user.email}. Осталось ждать ${RATE_LIMIT_MINUTES - minutesSinceCreation} минут" }
                throw IllegalStateException("Too many requests. Try again later")
            }
        }

        logger.debug { "Удаление активных токенов для пользователя ${user.id}" }
        tokenRepository.deleteActiveByUserId(user.id)

        val rawToken = TokenUtils.generateToken()
        val tokenHash = TokenUtils.sha256(rawToken)
        logger.debug { "Сгенерирован новый токен верификации" }

        tokenRepository.save(
            EmailVerificationToken(
                userId = user.id,
                tokenHash = tokenHash,
                expiresAt = Instant.now().plus(TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES)
            )
        )

        val link =
            "${properties.baseUrl}/auth/verify-email?token=$rawToken"

        logger.debug { "Отправляем письмо верификации на ${user.email}" }
        resendService.send(
            to = user.email,
            subject = "Подтверждение почты",
            html = """
                <h2>Подтвердите вашу почту</h2>
                <p>Спасибо за регистрацию в Antiplagiat!</p>
                <p><a href="$link" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;">Подтвердить почту</a></p>
                <p>Или скопируйте ссылку: <a href="$link">$link</a></p>
                <p>Ссылка действительна ${TOKEN_VALIDITY_MINUTES} минут</p>
            """.trimIndent()
        )
        logger.info { "Письмо верификации успешно отправлено на ${user.email}" }
    }

    @Transactional
    fun verify(rawToken: String): Long {
        logger.info { "Попытка верификации email с токеном" }
        val tokenHash = TokenUtils.sha256(rawToken)

        val token = tokenRepository.findByTokenHash(tokenHash)
            ?: run {
                logger.warn { "Токен не найден в базе данных" }
                throw IllegalArgumentException("Invalid token")
            }

        if (token.usedAt != null) {
            logger.warn { "Токен уже использован" }
            throw IllegalStateException("Token already used")
        }

        if (token.expiresAt.isBefore(Instant.now())) {
            logger.warn { "Токен истёк" }
            throw IllegalStateException("Token expired")
        }

        logger.debug { "Отмечаем email как подтверждённый для пользователя ${token.userId}" }
        userRepository.markEmailVerified(token.userId)

        token.usedAt = Instant.now()
        tokenRepository.save(token)
        logger.info { "Email успешно верифицирован для пользователя ${token.userId}" }

        return token.userId
    }

    @Transactional(readOnly = true)
    fun getUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }
    }

    fun generateToken(user: User): String {
        return tokenService.generateToken(user)
    }
}