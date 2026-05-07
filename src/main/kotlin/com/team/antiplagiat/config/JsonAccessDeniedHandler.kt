package com.team.antiplagiat.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.ApiError
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Instant

class JsonAccessDeniedHandler(private val objectMapper: ObjectMapper) : AccessDeniedHandler {
    override fun handle(request: HttpServletRequest?, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_FORBIDDEN
        val path = request?.requestURI ?: ""
        val body = ApiError(
            timestamp = Instant.now().toString(),
            status = HttpServletResponse.SC_FORBIDDEN,
            error = "Forbidden",
            message = accessDeniedException.message ?: "Access is denied",
            path = path
        )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}

