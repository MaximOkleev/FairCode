package com.team.antiplagiat.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import io.mockk.every
import io.mockk.mockk

class JsonSecurityHandlersTest {

    private val mapper = ObjectMapper()

    @Test
    fun `authentication entry point returns json error`() {
        val req = MockHttpServletRequest("GET", "/api/protected")
        val resp = MockHttpServletResponse()
        val entry = JsonAuthenticationEntryPoint(mapper)
        entry.commence(req, resp, BadCredentialsException("Bad creds"))

        assertEquals("application/json", resp.contentType)
        val body = mapper.readTree(resp.contentAsString)
        assertTrue(body.get("message").asText().contains("Bad creds"))
        assertTrue(body.has("timestamp"))
        assertTrue(body.has("traceId"))
    }

    @Test
    fun `access denied handler returns json error`() {
        val req = MockHttpServletRequest("GET", "/api/forbidden")
        val resp = MockHttpServletResponse()
        val handler = JsonAccessDeniedHandler(mapper)
        handler.handle(req, resp, AccessDeniedException("Denied"))

        assertEquals("application/json", resp.contentType)
        val body = mapper.readTree(resp.contentAsString)
        assertTrue(body.get("message").asText().contains("Denied"))
        assertTrue(body.has("timestamp"))
        assertTrue(body.has("traceId"))
    }

    @Test
    fun `authentication entry point handles null request`() {
        val resp = MockHttpServletResponse()
        val entry = JsonAuthenticationEntryPoint(mapper)
        entry.commence(null, resp, BadCredentialsException("no req"))

        assertEquals("application/json", resp.contentType)
        val body = mapper.readTree(resp.contentAsString)
        assertTrue(body.has("message"))
        assertTrue(body.has("timestamp"))
        assertTrue(body.has("traceId"))
    }

    @Test
    fun `access denied handles null request`() {
        val resp = MockHttpServletResponse()
        val handler = JsonAccessDeniedHandler(mapper)
        handler.handle(null, resp, AccessDeniedException("denied"))

        assertEquals("application/json", resp.contentType)
        val body = mapper.readTree(resp.contentAsString)
        assertTrue(body.has("message"))
        assertTrue(body.has("timestamp"))
        assertTrue(body.has("traceId"))
    }

    @Test
    fun `authentication entry point uses default message when exception message is null`() {
        val req = MockHttpServletRequest("GET", "/api/protected")
        val resp = MockHttpServletResponse()
        val entry = JsonAuthenticationEntryPoint(mapper)
        val ex = mockk<BadCredentialsException>()
        every { ex.message } returns null

        entry.commence(req, resp, ex)

        val body = mapper.readTree(resp.contentAsString)
        assertEquals("Full authentication is required to access this resource", body.get("message").asText())
    }

    @Test
    fun `access denied handler uses default message when exception message is null`() {
        val req = MockHttpServletRequest("GET", "/api/forbidden")
        val resp = MockHttpServletResponse()
        val handler = JsonAccessDeniedHandler(mapper)
        val ex = mockk<AccessDeniedException>()
        every { ex.message } returns null

        handler.handle(req, resp, ex)

        val body = mapper.readTree(resp.contentAsString)
        assertEquals("Access is denied", body.get("message").asText())
    }
}


