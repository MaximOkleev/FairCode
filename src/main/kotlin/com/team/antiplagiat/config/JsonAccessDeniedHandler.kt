package com.team.antiplagiat.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.filter.TraceIdFilter
import org.slf4j.MDC
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class JsonAccessDeniedHandler(private val objectMapper: ObjectMapper) : AccessDeniedHandler {
    override fun handle(request: HttpServletRequest?, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_FORBIDDEN
        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY) ?: request?.getHeader("X-Trace-Id")
        val body = SecurityErrorResponse(
            message = accessDeniedException.message ?: "Access is denied",
            traceId = traceId
        )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}

