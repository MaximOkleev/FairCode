package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayload
import com.team.antiplagiat.controller.dto.auth.ChangePasswordRequest
import com.team.antiplagiat.controller.dto.auth.LoginRequest
import com.team.antiplagiat.service.AuthService
import com.team.antiplagiat.service.EmailVerificationService
import com.team.antiplagiat.config.TokenService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
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
    fun `login throws on invalid credentials`() {
        val authService = mockk<AuthService>()
        val emailVerificationService = mockk<EmailVerificationService>()
        val tokenService = mockk<TokenService>()
        every { authService.authenticate("u", "p") } throws com.team.antiplagiat.exception.InvalidCredentialsException()

        val controller = AuthController(authService, emailVerificationService, tokenService)
        val req = LoginRequest(login = "u", password = "p")
        org.junit.jupiter.api.assertThrows<com.team.antiplagiat.exception.InvalidCredentialsException> {
            controller.login(req)
        }
    }

    @Test
    fun `changePassword returns 200 when token payload exists`() {
        val authService = mockk<AuthService>()
        val emailVerificationService = mockk<EmailVerificationService>()
        val tokenService = mockk<TokenService>()
        val httpRequest = mockk<HttpServletRequest>()
        val payload = TokenPayload(
            userId = 7L,
            login = "user",
            email = "user@example.com",
            role = "BASIC"
        )
        every { httpRequest.getAttribute("tokenPayload") } returns payload
        every { authService.changePassword(7L, "old", "new") } just runs

        val controller = AuthController(authService, emailVerificationService, tokenService)
        val resp = controller.changePassword(ChangePasswordRequest("old", "new"), httpRequest)

        assertEquals(200, resp.statusCodeValue)
        assertEquals("Password changed successfully", resp.body?.get("message"))
        verify(exactly = 1) { authService.changePassword(7L, "old", "new") }
    }

    @Test
    fun `changePassword returns 401 when token payload missing`() {
        val authService = mockk<AuthService>()
        val emailVerificationService = mockk<EmailVerificationService>()
        val tokenService = mockk<TokenService>()
        val httpRequest = mockk<HttpServletRequest>()
        every { httpRequest.getAttribute("tokenPayload") } returns null

        val controller = AuthController(authService, emailVerificationService, tokenService)
        val resp = controller.changePassword(ChangePasswordRequest("old", "new"), httpRequest)

        assertEquals(401, resp.statusCodeValue)
        verify(exactly = 0) { authService.changePassword(any(), any(), any()) }
    }
}
