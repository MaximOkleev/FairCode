package com.team.antiplagiat.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class JsonAuthenticationEntryPoint(private val objectMapper: ObjectMapper) : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest?, response: HttpServletResponse, authException: AuthenticationException) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        val traceId = request?.getHeader("X-Trace-Id")
        val body = SecurityErrorResponse(
            message = authException.message ?: "Full authentication is required to access this resource",
            traceId = traceId
        )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}

