package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.register.RegisterRequest
import com.team.antiplagiat.controller.dto.register.RegisterResponse
import com.team.antiplagiat.service.RegisterService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RegisterControllerTest {

    @Test
    fun `register returns created status and response on success`() {
        val registerService = mockk<RegisterService>()
        val response = RegisterResponse(userId = 123L, email = "test@example.com", token = "token123")
        every { registerService.register(any()) } returns response

        val controller = RegisterController(registerService)
        val request = RegisterRequest(email = "test@example.com", password = "password123")
        val result = controller.register(request)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(response, result.body)
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
    fun `register returns internal server error on unexpected exception`() {
        val registerService = mockk<RegisterService>()
        every { registerService.register(any()) } throws RuntimeException("Database error")

        val controller = RegisterController(registerService)
        val request = RegisterRequest(email = "test@example.com", password = "password123")
        val result = controller.register(request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
    }
}

