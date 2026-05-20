package com.team.antiplagiat.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.env.Environment
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class SecurityConfigUnitTest {

    @Test
    fun `userDetailsService returns in-memory manager when provider memory`() {
        val env = mockk<Environment>()
        every { env.getProperty("app.security.user-provider", "database") } returns "memory"
        every { env.getProperty("app.security.memory.username", "admin") } returns "adm"
        every { env.getProperty("app.security.memory.password", "admin") } returns "pwd"
        every { env.getProperty("app.security.memory.roles", "ADMIN") } returns "ADMIN"

        val cfg = SecurityConfig(env)
        val encoder = cfg.passwordEncoder() as BCryptPasswordEncoder
        val userDetails = cfg.userDetailsService(encoder, mockk())
        assertTrue(userDetails is InMemoryUserDetailsManager)
    }

    @Test
    fun `userDetailsService returns database provider when configured`() {
        val env = mockk<Environment>()
        every { env.getProperty("app.security.user-provider", "database") } returns "database"

        val cfg = SecurityConfig(env)
        val encoder = cfg.passwordEncoder()
        val dbService = mockk<DatabaseUserDetailsService>()
        val userDetails = cfg.userDetailsService(encoder, dbService)
        assertSame(dbService, userDetails)
    }

    @Test
    fun `userDetailsService falls back to database when provider is unknown`() {
        val env = mockk<Environment>()
        every { env.getProperty("app.security.user-provider", "database") } returns "unknown"

        val cfg = SecurityConfig(env)
        val encoder = cfg.passwordEncoder()
        val dbService = mockk<DatabaseUserDetailsService>()
        val userDetails = cfg.userDetailsService(encoder, dbService)

        assertSame(dbService, userDetails)
    }
}

