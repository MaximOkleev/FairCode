package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.auth.LoginRequest
import com.team.antiplagiat.controller.dto.auth.LoginResponse
import com.team.antiplagiat.service.AuthService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Аутентификация")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    @Operation(summary = "Войти по логину и паролю, получить токен")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        logger.info { "POST /api/auth/login - попытка входа: ${request.login}" }
        val token = authService.authenticate(request.login, request.password)
        logger.info { "Пользователь успешно вошел: ${request.login}" }
        return ResponseEntity.ok(LoginResponse(token = token))
    }
}

