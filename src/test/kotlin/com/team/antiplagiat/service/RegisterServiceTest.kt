package com.team.antiplagiat.service

import com.team.antiplagiat.controller.dto.register.RegisterRequest
import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.models.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.security.crypto.password.PasswordEncoder

class RegisterServiceTest {

    @Test
    fun `register sends verification email and returns response`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val emailVerificationService = mockk<EmailVerificationService>()
        val meter = SimpleMeterRegistry()

        every { repo.findByEmail("e@x") } returns null
        every { encoder.encode("pwd") } returns "hashed"
        every { repo.save(any()) } answers { firstArg() as User }
        val savedUser = User(id = 123, login = "e", email = "e@x", passwordHash = "hashed")
        every { repo.save(match { it.email == "e@x" }) } returns savedUser
        every { emailVerificationService.sendVerification("e@x") } just runs

        val service = RegisterService(repo, encoder, meter, emailVerificationService)
        val resp = service.register(RegisterRequest(email = "e@x", password = "pwd"))
        assertEquals(123L, resp.userId)
        assertEquals("e@x", resp.email)
        assertTrue(resp.emailVerificationRequired)
    }

    @Test
    fun `register throws when email exists`() {
        val repo = mockk<UserRepository>()
        val encoder = mockk<PasswordEncoder>()
        val emailVerificationService = mockk<EmailVerificationService>()
        val meter = SimpleMeterRegistry()

        every { repo.findByEmail("a@b") } returns User(id = 1, login = "l", email = "a@b")
        val service = RegisterService(repo, encoder, meter, emailVerificationService)
        assertThrows(IllegalArgumentException::class.java) { service.register(RegisterRequest(email = "a@b", password = "p")) }
    }
}

