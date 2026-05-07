package com.team.antiplagiat.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(private val tokenService: TokenService) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (!authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ")) {
            val token = authHeader.removePrefix("Bearer ").trim()
            val payload = tokenService.parseToken(token)
            if (payload != null) {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${payload.role}"))
                val auth = UsernamePasswordAuthenticationToken(payload.login, null, authorities)
                SecurityContextHolder.getContext().authentication = auth
                request.setAttribute("tokenPayload", payload)
            }
        }

        filterChain.doFilter(request, response)
    }
}

