package com.team.antiplagiat.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder as SpringSecurityContextHolder

/**
 * Helper для получения TokenPayload из SecurityContext или HttpServletRequest
 */
object TokenPayloadExtractor {

    /**
     * Извлекает TokenPayload из HttpServletRequest (установлен JwtAuthenticationFilter)
     */
    fun getTokenPayload(request: HttpServletRequest): TokenPayload? {
        return request.getAttribute("tokenPayload") as? TokenPayload
    }

    /**
     * Извлекает userId из SecurityContext (principal)
     */
    fun getUserId(): Long? {
        return SpringSecurityContextHolder.getContext()?.authentication?.principal as? Long
    }

    /**
     * Проверяет, аутентифицирован ли пользователь
     */
    fun isAuthenticated(): Boolean {
        return SpringSecurityContextHolder.getContext()?.authentication?.isAuthenticated == true
    }
}

