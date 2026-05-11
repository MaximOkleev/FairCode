package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.auth.LoginRequest
import com.team.antiplagiat.controller.dto.auth.LoginResponse
import com.team.antiplagiat.controller.dto.auth.VerifyEmailRequest
import com.team.antiplagiat.controller.dto.auth.VerifyEmailResponse
import com.team.antiplagiat.service.AuthService
import com.team.antiplagiat.service.EmailVerificationService
import com.team.antiplagiat.config.TokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Аутентификация")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val tokenService: TokenService
) {

    @PostMapping("/login")
    @Operation(summary = "Войти по логину и паролю, получить токен")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        logger.info { "POST /api/auth/login - попытка входа: ${request.login}" }
        return try {
            val token = authService.authenticate(request.login, request.password)
            logger.info { "Пользователь успешно вошел: ${request.login}" }
            ResponseEntity.ok(LoginResponse(token = token))
        } catch (e: com.team.antiplagiat.service.EmailNotVerifiedException) {
            logger.info { "Отправляем письмо верификации для ${e.email}" }
            try {
                emailVerificationService.sendVerification(e.email)
                ResponseEntity.status(403).body(LoginResponse(token = "", message = e.message))
            } catch (rateLimitException: IllegalStateException) {
                logger.warn { "Слишком частая попытка отправки письма: ${rateLimitException.message}" }
                ResponseEntity.status(429).body(LoginResponse(token = "", message = rateLimitException.message))
            }
        } catch (e: IllegalArgumentException) {
            logger.warn { "Ошибка аутентификации: ${e.message}" }
            ResponseEntity.status(401).body(LoginResponse(token = "", message = "Invalid email or password"))
        }
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Подтвердить email по ссылке из письма")
    fun verifyEmailFromLink(@RequestParam token: String): ResponseEntity<VerifyEmailResponse> {
        return try {
            logger.info { "GET /api/auth/verify-email - подтверждение email по ссылке" }
            val userId = emailVerificationService.verify(token)
            logger.info { "Email успешно подтверждён по ссылке для пользователя $userId" }
            val user = emailVerificationService.getUserById(userId)
            val authToken = tokenService.generateToken(user)

            ResponseEntity.ok(
                VerifyEmailResponse(
                    message = "Email verified successfully",
                    success = true,
                    userId = userId,
                    token = authToken
                )
            )
        } catch (e: IllegalArgumentException) {
            logger.warn { "Ошибка верификации: ${e.message}" }
            ResponseEntity.badRequest().body(
                VerifyEmailResponse(
                    message = "Invalid Token: ${e.message}",
                    success = false
                )
            )
        } catch (e: IllegalStateException) {
            logger.warn { "Ошибка верификации: ${e.message}" }
            ResponseEntity.badRequest().body(
                VerifyEmailResponse(
                    message = e.message ?: "Verification failed",
                    success = false
                )
            )
        }
    }
}
