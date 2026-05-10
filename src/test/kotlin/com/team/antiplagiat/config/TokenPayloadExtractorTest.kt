package com.team.antiplagiat.config

import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder as SpringSecurityContextHolder
import io.mockk.every
import io.mockk.mockk

class TokenPayloadExtractorTest {

    @AfterEach
    fun tearDown() {
        SpringSecurityContextHolder.clearContext()
    }

    @Test
    fun `getTokenPayload returns payload from request attribute`() {
        val request = mockk<HttpServletRequest>()
        val payload = TokenPayload(1L, "login", "mail@example.com", "ADMIN")
        every { request.getAttribute("tokenPayload") } returns payload

        assertEquals(payload, TokenPayloadExtractor.getTokenPayload(request))
    }

    @Test
    fun `getTokenPayload returns null when attribute missing`() {
        val request = mockk<HttpServletRequest>()
        every { request.getAttribute("tokenPayload") } returns null

        assertNull(TokenPayloadExtractor.getTokenPayload(request))
    }

    @Test
    fun `getUserId returns principal from security context`() {
        SpringSecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(42L, null, emptyList())

        assertEquals(42L, TokenPayloadExtractor.getUserId())
    }

    @Test
    fun `isAuthenticated returns true when authentication exists`() {
        SpringSecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(42L, null, emptyList())

        assertTrue(TokenPayloadExtractor.isAuthenticated())
    }

    @Test
    fun `isAuthenticated returns false when security context is empty`() {
        SpringSecurityContextHolder.clearContext()

        assertFalse(TokenPayloadExtractor.isAuthenticated())
        assertNull(TokenPayloadExtractor.getUserId())
    }
}
