package com.team.antiplagiat.service

import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.config.TokenService
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {

    @Test
    fun `authenticate returns token when credentials valid`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()
        val securityAlertService = mockk<SecurityAlertService>()

        val user = User(id = 5, login = "login", email = "a@b", emailVerified = true)
        every { repo.findByLogin("login") } returns user
        every { repo.findByEmail("login") } returns null
        every { encoder.matches("pass", user.passwordHash) } returns true
        every { securityAlertService.sendLoginAlert(user) } just runs
        every { tokenService.generateToken(user) } returns "tok"

        val service = AuthService(repo, encoder, tokenService, securityAlertService, SimpleMeterRegistry())
        val token = service.authenticate("login", "pass")
        assertEquals("tok", token)
        verify(exactly = 1) { securityAlertService.sendLoginAlert(user) }
    }

    @Test
    fun `authenticate returns token when login matches email`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()
        val securityAlertService = mockk<SecurityAlertService>()

        val user = User(id = 5, login = "ivan", email = "ivan@example.com", emailVerified = true)
        every { repo.findByLogin("ivan@example.com") } returns null
        every { repo.findByEmail("ivan@example.com") } returns user
        every { encoder.matches("pass", user.passwordHash) } returns true
        every { securityAlertService.sendLoginAlert(user) } just runs
        every { tokenService.generateToken(user) } returns "tok"

        val service = AuthService(repo, encoder, tokenService, securityAlertService, SimpleMeterRegistry())
        val token = service.authenticate("ivan@example.com", "pass")

        assertEquals("tok", token)
        verify(exactly = 1) { securityAlertService.sendLoginAlert(user) }
    }

    @Test
    fun `authenticate still returns token when security alert fails`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()
        val securityAlertService = mockk<SecurityAlertService>()

        val user = User(id = 5, login = "login", email = "a@b", emailVerified = true)
        every { repo.findByLogin("login") } returns user
        every { repo.findByEmail("login") } returns null
        every { encoder.matches("pass", user.passwordHash) } returns true
        every { securityAlertService.sendLoginAlert(user) } throws IllegalStateException("resend is down")
        every { tokenService.generateToken(user) } returns "tok"

        val service = AuthService(repo, encoder, tokenService, securityAlertService, SimpleMeterRegistry())
        val token = service.authenticate("login", "pass")

        assertEquals("tok", token)
        verify(exactly = 1) { securityAlertService.sendLoginAlert(user) }
    }

    @Test
    fun `authenticate throws when user not found`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()
        val securityAlertService = mockk<SecurityAlertService>()
        every { repo.findByLogin(any()) } returns null
        every { repo.findByEmail(any()) } returns null
        val service = AuthService(repo, encoder, tokenService, securityAlertService, SimpleMeterRegistry())
        org.junit.jupiter.api.assertThrows<com.team.antiplagiat.exception.InvalidCredentialsException> { service.authenticate("no", "p") }
        verify(exactly = 0) { securityAlertService.sendLoginAlert(any()) }
    }

    @Test
    fun `authenticate throws when password invalid`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()
        val securityAlertService = mockk<SecurityAlertService>()
        val user = User(id = 1, login = "l", email = "e")
        every { repo.findByLogin("l") } returns user
        every { repo.findByEmail("l") } returns null
        every { encoder.matches("bad", user.passwordHash) } returns false
        val service = AuthService(repo, encoder, tokenService, securityAlertService, SimpleMeterRegistry())
        org.junit.jupiter.api.assertThrows<com.team.antiplagiat.exception.InvalidCredentialsException> { service.authenticate("l", "bad") }
        verify(exactly = 0) { securityAlertService.sendLoginAlert(any()) }
    }
}
