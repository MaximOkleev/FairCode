package com.team.antiplagiat.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Фильтр для проставления уникального traceId каждому входящему запросу.
 *
 * traceId добавляется в MDC (Mapped Diagnostic Context), откуда логгер
 * автоматически включает его в каждую строку лога для данного запроса.
 * Также traceId возвращается клиенту в заголовке X-Trace-Id.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TraceIdFilter : OncePerRequestFilter() {

    companion object {
        const val TRACE_ID_KEY = "traceId"
        const val TRACE_ID_HEADER = "X-Trace-Id"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Берём traceId из заголовка запроса или генерируем новый
        val traceId = request.getHeader(TRACE_ID_HEADER)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString().replace("-", "").take(16)

        try {
            MDC.put(TRACE_ID_KEY, traceId)
            // Возвращаем traceId клиенту в заголовке ответа
            response.setHeader(TRACE_ID_HEADER, traceId)

            logger.debug (
                "Входящий запрос: ${request.method} ${request.requestURI} | traceId=$traceId"
            )

            filterChain.doFilter(request, response)

            logger.debug (
                "Ответ отправлен: status=${response.status} | traceId=$traceId"
            )
        } finally {
            // ВАЖНО: всегда очищаем MDC, иначе traceId «утечёт» в другие запросы
            MDC.remove(TRACE_ID_KEY)
        }
    }
}