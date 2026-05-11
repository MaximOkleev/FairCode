package com.team.antiplagiat.controller.dto.register

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegisterDtoTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `invalid email and short password produce violations`() {
        val req = RegisterRequest(email = "bad", password = "123")

        val violations = validator.validate(req)
        val messages = violations.map { it.message }

        assertEquals(2, violations.size)
        assertTrue(messages.any { it.contains("Некорректный формат email") })
        assertTrue(messages.any { it.contains("минимум 6 символов") })
    }

    @Test
    fun `valid register request has no violations`() {
        val req = RegisterRequest(email = "user@example.com", password = "pass123")

        val violations = validator.validate(req)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `register response keeps fields`() {
        val response = RegisterResponse(
            userId = 42L,
            email = "user@example.com",
            message = "Пользователь зарегистрирован. Проверьте почту для подтверждения email",
            emailVerificationRequired = true
        )

        assertEquals(42L, response.userId)
        assertEquals("user@example.com", response.email)
        assertTrue(response.emailVerificationRequired)
    }
}

