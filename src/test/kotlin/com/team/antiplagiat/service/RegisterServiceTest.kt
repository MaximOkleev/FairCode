package com.team.antiplagiat.service

import com.team.antiplagiat.controller.dto.register.RegisterRequest
import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.models.User
import com.team.antiplagiat.config.TokenService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.security.crypto.password.PasswordEncoder

class RegisterServiceTest {

    @Test
    fun `register returns response with token`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()
        val meter = SimpleMeterRegistry()

        every { repo.findByEmail("e@x") } returns null
        every { encoder.encode("pwd") } returns "hashed"
        every { repo.save(any()) } answers { firstArg() as User }
        val savedUser = User(id = 123, login = "e", email = "e@x", passwordHash = "hashed")
        every { repo.save(match { it.email == "e@x" }) } returns savedUser
        every { tokenService.generateToken(savedUser) } returns "tok123"

        val service = RegisterService(repo, encoder, meter, tokenService)
        val resp = service.register(RegisterRequest(email = "e@x", password = "pwd"))
        assertEquals(123L, resp.userId)
        assertEquals("e@x", resp.email)
        assertEquals("tok123", resp.token)
    }

    @Test
    fun `register throws when email exists`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val tokenService = mockk<TokenService>()
        val meter = SimpleMeterRegistry()

        every { repo.findByEmail("a@b") } returns User(id = 1, login = "l", email = "a@b")
        val service = RegisterService(repo, encoder, meter, tokenService)
        assertThrows(IllegalArgumentException::class.java) { service.register(RegisterRequest(email = "a@b", password = "p")) }
    }
}

