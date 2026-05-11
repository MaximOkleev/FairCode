package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.auth.LoginRequest
import com.team.antiplagiat.service.AuthService
import com.team.antiplagiat.service.EmailVerificationService
import com.team.antiplagiat.config.TokenService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class AuthControllerTest {

    @Test
    fun `login returns token from service`() {
        val authService = mockk<AuthService>()
        val emailVerificationService = mockk<EmailVerificationService>()
        val tokenService = mockk<TokenService>()
        every { authService.authenticate("u", "p") } returns "tkn"

        val controller = AuthController(authService, emailVerificationService, tokenService)
        val req = LoginRequest(login = "u", password = "p")
        val resp: ResponseEntity<*> = controller.login(req)
        assertEquals(200, resp.statusCodeValue)
        val body = resp.body as? com.team.antiplagiat.controller.dto.auth.LoginResponse
        assertEquals("tkn", body?.token)
    }

    @Test
    fun `login returns 401 on invalid credentials`() {
        val authService = mockk<AuthService>()
        val emailVerificationService = mockk<EmailVerificationService>()
        val tokenService = mockk<TokenService>()
        every { authService.authenticate("u", "p") } throws IllegalArgumentException("bad creds")

        val controller = AuthController(authService, emailVerificationService, tokenService)
        val req = LoginRequest(login = "u", password = "p")
        val resp: ResponseEntity<*> = controller.login(req)
        assertEquals(401, resp.statusCodeValue)
    }
}
