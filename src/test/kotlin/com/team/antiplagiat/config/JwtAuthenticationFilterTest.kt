package com.team.antiplagiat.config

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import jakarta.servlet.FilterChain
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthenticationFilterTest {

    @BeforeEach
    fun setup() {
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `filter sets authentication and request attribute when token valid`() {
        val tokenService = mockk<TokenService>()
        val payload = TokenPayload(userId = 1, login = "u", email = "e@x", role = "BASIC")
        every { tokenService.parseToken("valid-token") } returns payload

        val filter = JwtAuthenticationFilter(tokenService)

        val req = MockHttpServletRequest()
        val resp = MockHttpServletResponse()
        req.addHeader("Authorization", "Bearer valid-token")

        var chainCalled = false
        val chain = object : FilterChain {
            override fun doFilter(request: jakarta.servlet.ServletRequest?, response: jakarta.servlet.ServletResponse?) {
                chainCalled = true
            }
        }

        filter.doFilter(req, resp, chain)

        assertTrue(chainCalled)
        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertEquals("1", auth.name)
        val attr = req.getAttribute("tokenPayload") as? TokenPayload
        assertNotNull(attr)
        assertEquals(1L, attr!!.userId)
    }

    @Test
    fun `filter does nothing when no header`() {
        val tokenService = mockk<TokenService>()
        val filter = JwtAuthenticationFilter(tokenService)
        val req = MockHttpServletRequest()
        val resp = MockHttpServletResponse()
        var chainCalled = false
        val chain = object : FilterChain {
            override fun doFilter(request: jakarta.servlet.ServletRequest?, response: jakarta.servlet.ServletResponse?) {
                chainCalled = true
            }
        }
        filter.doFilter(req, resp, chain)
        assertTrue(chainCalled)
        assertNull(SecurityContextHolder.getContext().authentication)
        assertNull(req.getAttribute("tokenPayload"))
    }

    @Test
    fun `filter ignores invalid token`() {
        val tokenService = mockk<TokenService>()
        every { tokenService.parseToken("bad-token") } returns null

        val filter = JwtAuthenticationFilter(tokenService)

        val req = MockHttpServletRequest()
        val resp = MockHttpServletResponse()
        req.addHeader("Authorization", "Bearer bad-token")

        var chainCalled = false
        val chain = object : FilterChain {
            override fun doFilter(request: jakarta.servlet.ServletRequest?, response: jakarta.servlet.ServletResponse?) {
                chainCalled = true
            }
        }

        filter.doFilter(req, resp, chain)

        assertTrue(chainCalled)
        assertNull(SecurityContextHolder.getContext().authentication)
        assertNull(req.getAttribute("tokenPayload"))
    }
}
