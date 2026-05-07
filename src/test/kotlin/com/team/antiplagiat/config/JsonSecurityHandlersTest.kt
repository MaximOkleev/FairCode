package com.team.antiplagiat.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException

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
        assertEquals(401, body.get("status").asInt())
        assertEquals("Unauthorized", body.get("error").asText())
        assertTrue(body.get("message").asText().contains("Bad creds"))
        assertEquals("/api/protected", body.get("path").asText())
    }

    @Test
    fun `access denied handler returns json error`() {
        val req = MockHttpServletRequest("GET", "/api/forbidden")
        val resp = MockHttpServletResponse()
        val handler = JsonAccessDeniedHandler(mapper)
        handler.handle(req, resp, AccessDeniedException("Denied"))

        assertEquals("application/json", resp.contentType)
        val body = mapper.readTree(resp.contentAsString)
        assertEquals(403, body.get("status").asInt())
        assertEquals("Forbidden", body.get("error").asText())
        assertTrue(body.get("message").asText().contains("Denied"))
        assertEquals("/api/forbidden", body.get("path").asText())
    }

    @Test
    fun `authentication entry point handles null request`() {
        val resp = MockHttpServletResponse()
        val entry = JsonAuthenticationEntryPoint(mapper)
        entry.commence(null, resp, BadCredentialsException("no req"))

        assertEquals("application/json", resp.contentType)
        val body = mapper.readTree(resp.contentAsString)
        assertEquals(401, body.get("status").asInt())
        assertEquals("", body.get("path").asText())
    }

    @Test
    fun `access denied handles null request`() {
        val resp = MockHttpServletResponse()
        val handler = JsonAccessDeniedHandler(mapper)
        handler.handle(null, resp, AccessDeniedException("denied"))

        assertEquals("application/json", resp.contentType)
        val body = mapper.readTree(resp.contentAsString)
        assertEquals(403, body.get("status").asInt())
        assertEquals("", body.get("path").asText())
    }
}


