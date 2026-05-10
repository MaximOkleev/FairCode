package com.team.antiplagiat.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment
import java.util.Date

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

    @Test
    fun `parse token without email and role uses defaults`() {
        val secret = "test-secret-123"
        val env = MockEnvironment()
        env.setProperty("app.security.jwt.secret", secret)
        val tokenService = TokenService(env)

        val token = JWT.create()
            .withSubject("johndoe")
            .withClaim("userId", 42L)
            .withIssuedAt(Date())
            .sign(Algorithm.HMAC256(secret))

        val payload = tokenService.parseToken(token)

        assertNotNull(payload)
        assertEquals(42L, payload!!.userId)
        assertEquals("johndoe", payload.login)
        assertEquals("", payload.email)
        assertEquals("BASIC", payload.role)
    }

    @Test
    fun `parse token without user id returns null`() {
        val secret = "test-secret-123"
        val env = MockEnvironment()
        env.setProperty("app.security.jwt.secret", secret)
        val tokenService = TokenService(env)

        val token = JWT.create()
            .withSubject("johndoe")
            .withClaim("email", "john@example.com")
            .withClaim("role", "BASIC")
            .withIssuedAt(Date())
            .sign(Algorithm.HMAC256(secret))

        val payload = tokenService.parseToken(token)

        assertNull(payload)
    }

    @Test
    fun `parse token without subject returns null`() {
        val secret = "test-secret-123"
        val env = MockEnvironment()
        env.setProperty("app.security.jwt.secret", secret)
        val tokenService = TokenService(env)

        val token = JWT.create()
            .withClaim("userId", 42L)
            .withClaim("email", "john@example.com")
            .withClaim("role", "BASIC")
            .withIssuedAt(Date())
            .sign(Algorithm.HMAC256(secret))

        val payload = tokenService.parseToken(token)

        assertNull(payload)
    }
}

