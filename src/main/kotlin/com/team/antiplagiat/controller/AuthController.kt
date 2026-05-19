package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.auth.ChangePasswordRequest
import com.team.antiplagiat.controller.dto.auth.LoginRequest
import com.team.antiplagiat.controller.dto.auth.LoginResponse
import com.team.antiplagiat.controller.dto.auth.VerifyEmailResponse
import com.team.antiplagiat.service.AuthService
import com.team.antiplagiat.service.EmailVerificationService
import com.team.antiplagiat.config.TokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
        logger.info { "POST /api/auth/login - попытка входа" }
        val token = authService.authenticate(request.login, request.password)
        logger.info { "Пользователь успешно вошел" }
        return ResponseEntity.ok(LoginResponse(token = token))
    }

    @PostMapping("/change-password")
    @Operation(summary = "Изменить пароль текущего пользователя")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "POST /api/auth/change-password - смена пароля для пользователя ${payload.userId}" }
        authService.changePassword(payload.userId, request.oldPassword, request.newPassword)
        return ResponseEntity.ok(mapOf("message" to "Password changed successfully"))
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Подтвердить email по ссылке из письма")
    fun verifyEmailFromLink(@RequestParam token: String): ResponseEntity<VerifyEmailResponse> {
        logger.info { "GET /api/auth/verify-email - подтверждение email по ссылке" }
        val userId = emailVerificationService.verify(token)
        logger.info { "Email успешно подтверждён по ссылке для пользователя $userId" }
        val user = emailVerificationService.getUserById(userId)
        val authToken = tokenService.generateToken(user)

        return ResponseEntity.ok(
            VerifyEmailResponse(
                message = "Email verified successfully",
                success = true,
                userId = userId,
                token = authToken
            )
        )
    }
}
