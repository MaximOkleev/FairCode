package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.register.RegisterRequest
import com.team.antiplagiat.controller.dto.register.RegisterResponse
import com.team.antiplagiat.service.RegisterService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RegisterControllerTest {

    @Test
    fun `register returns created status and response on success`() {
        val registerService = mockk<RegisterService>()
        val response = RegisterResponse(
            userId = 123L,
            email = "test@example.com",
            message = "Пользователь зарегистрирован. Проверьте почту для подтверждения email",
            emailVerificationRequired = true
        )
        every { registerService.register(any()) } returns response

        val controller = RegisterController(registerService)
        val request = RegisterRequest(email = "test@example.com", password = "password123")
        val result = controller.register(request)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(response, result.body)
        assertTrue(result.body?.emailVerificationRequired ?: false)
    }

    @Test
    fun `register returns bad request when email already exists`() {
        val registerService = mockk<RegisterService>()
        every { registerService.register(any()) } throws IllegalArgumentException("Email already registered")

        val controller = RegisterController(registerService)
        val request = RegisterRequest(email = "existing@example.com", password = "password123")
        val result = controller.register(request)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @Test
    fun `register returns too many requests on rate limit`() {
        val registerService = mockk<RegisterService>()
        every { registerService.register(any()) } throws IllegalStateException("Too many requests. Try again later")

        val controller = RegisterController(registerService)
        val request = RegisterRequest(email = "test@example.com", password = "password123")
        val result = controller.register(request)

        assertEquals(429, result.statusCodeValue)
    }
}
