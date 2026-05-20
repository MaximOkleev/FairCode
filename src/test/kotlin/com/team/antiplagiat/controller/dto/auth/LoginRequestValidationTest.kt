package com.team.antiplagiat.controller.dto.auth

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LoginRequestValidationTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `blank login and password produce violations`() {
        val req = LoginRequest(login = "", password = "")
        val violations = validator.validate(req)
        assertEquals(2, violations.size)
        val messages = violations.map { it.message }
        assertTrue(messages.any { it.contains("Login не может быть пустым") })
        assertTrue(messages.any { it.contains("Пароль не может быть пустым") })
    }

    @Test
    fun `valid login request has no violations`() {
        val req = LoginRequest(login = "user", password = "pass")
        val violations = validator.validate(req)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `login response keeps token`() {
        val response = LoginResponse(token = "jwt-token")
        assertEquals("jwt-token", response.token)
    }
}

