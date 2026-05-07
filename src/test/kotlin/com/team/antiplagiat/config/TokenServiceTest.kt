package com.team.antiplagiat.config

import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment

class TokenServiceTest {

    @Test
    fun `generate and parse token should return same payload`() {
        val env = MockEnvironment()
        env.setProperty("app.security.jwt.secret", "test-secret-123")
        env.setProperty("app.security.jwt.expiration-seconds", "3600")

        val tokenService = TokenService(env)

        val user = User(id = 42, login = "johndoe", email = "john@example.com", role = User.Role.BASIC)

        val token = tokenService.generateToken(user)
        assertNotNull(token)

        val payload = tokenService.parseToken(token)
        assertNotNull(payload)
        assertEquals(42L, payload!!.userId)
        assertEquals("johndoe", payload.login)
        assertEquals("john@example.com", payload.email)
        assertEquals("BASIC", payload.role)
    }

    @Test
    fun `parse invalid token returns null`() {
        val env = MockEnvironment()
        env.setProperty("app.security.jwt.secret", "test-secret-123")
        val tokenService = TokenService(env)

        val payload = tokenService.parseToken("this.is.not.a.valid.token")
        assertNull(payload)
    }
}

