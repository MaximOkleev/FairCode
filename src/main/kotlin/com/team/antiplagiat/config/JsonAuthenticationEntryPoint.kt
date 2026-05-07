package com.team.antiplagiat.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.ApiError
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Instant

class JsonAuthenticationEntryPoint(private val objectMapper: ObjectMapper) : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest?, response: HttpServletResponse, authException: AuthenticationException) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        val path = request?.requestURI ?: ""
        val body = ApiError(
            timestamp = Instant.now().toString(),
            status = HttpServletResponse.SC_UNAUTHORIZED,
            error = "Unauthorized",
            message = authException.message ?: "Full authentication is required to access this resource",
            path = path
        )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}

