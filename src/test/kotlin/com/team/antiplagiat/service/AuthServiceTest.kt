package com.team.antiplagiat.service

import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.config.TokenService
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {

    @Test
    fun `authenticate returns token when credentials valid`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()

        val user = User(id = 5, login = "login", email = "a@b", emailVerified = true)
        every { repo.findByLogin("login") } returns user
        every { encoder.matches("pass", user.passwordHash) } returns true
        every { tokenService.generateToken(user) } returns "tok"

        val service = AuthService(repo, encoder, tokenService, SimpleMeterRegistry())
        val token = service.authenticate("login", "pass")
        assertEquals("tok", token)
    }

    @Test
    fun `authenticate throws when user not found`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()
        every { repo.findByLogin(any()) } returns null
        val service = AuthService(repo, encoder, tokenService, SimpleMeterRegistry())
        assertThrows(IllegalArgumentException::class.java) { service.authenticate("no", "p") }
    }

    @Test
    fun `authenticate throws when password invalid`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()
        val user = User(id = 1, login = "l", email = "e")
        every { repo.findByLogin("l") } returns user
        every { encoder.matches("bad", user.passwordHash) } returns false
        val service = AuthService(repo, encoder, tokenService, SimpleMeterRegistry())
        assertThrows(IllegalArgumentException::class.java) { service.authenticate("l", "bad") }
    }
}
