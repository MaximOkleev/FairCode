package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.register.RegisterRequest
import com.team.antiplagiat.controller.dto.register.RegisterResponse
import com.team.antiplagiat.service.RegisterService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/register")
@Tag(name = "Register", description = "Регистрация пользователей")
class RegisterController(private val registerService: RegisterService) {

    @PostMapping
    @Operation(summary = "Регистрация нового пользователя", description = "Принимает email и пароль.")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        logger.info { "POST /api/register - регистрация нового пользователя: ${request.email}" }
        logger.debug { "Валидация данных: email=${request.email}, пароль скрыт" }

        return try {
            val response = registerService.register(request)
            logger.info { "Пользователь зарегистрирован: ${request.email}, userId=${response.userId}" }
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            logger.warn { "Регистрация отклонена: ${e.message}" }
            ResponseEntity.badRequest().build()
        } catch (e: IllegalStateException) {
            logger.warn { "Слишком частая попытка отправки письма: ${e.message}" }
            ResponseEntity.status(429).body(
                RegisterResponse(
                    userId = 0,
                    email = request.email,
                    message = e.message ?: "Too many requests. Try again later",
                    emailVerificationRequired = true
                )
            )
        } catch (e: Exception) {
            logger.error { "Ошибка при регистрации пользователя ${request.email}: ${e.message}" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}