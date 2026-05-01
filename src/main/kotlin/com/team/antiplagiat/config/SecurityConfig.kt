package com.team.antiplagiat.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

private val logger = KotlinLogging.logger {}

@Configuration
@EnableWebSecurity
class SecurityConfig(private val env: Environment) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(
        passwordEncoder: PasswordEncoder,
        databaseUserDetailsService: DatabaseUserDetailsService
    ): UserDetailsService {
        val provider = env.getProperty("app.security.user-provider", "database")

        return when (provider) {
            "memory" -> {
                val username = env.getProperty("app.security.memory.username", "admin")
                val password = env.getProperty("app.security.memory.password", "admin")
                val rolesStr = env.getProperty("app.security.memory.roles", "ADMIN")
                val roles = rolesStr.split(",").map { it.trim() }.toTypedArray()

                val user = User.withUsername(username)
                    .password(passwordEncoder.encode(password))
                    .roles(*roles)
                    .build()

                logger.info { "In-memory user '$username' created with roles: ${roles.toList()}" }
                logger.warn { "Using IN-MEMORY authentication. This should NOT be used in production!" }

                InMemoryUserDetailsManager(user)
            }
            "database" -> {
                logger.info { "Using DATABASE authentication" }
                databaseUserDetailsService
            }
            else -> {
                logger.error { "Unknown user provider: $provider. Defaulting to database" }
                databaseUserDetailsService
            }
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val enabled = env.getProperty("app.security.enabled", Boolean::class.java, false)
        http.csrf { it.disable() }
        if (!enabled) {
            http.authorizeHttpRequests { it.anyRequest().permitAll() }
            return http.build()
        }

        http.authorizeHttpRequests { auth ->
            auth.requestMatchers(
                "/actuator/health",
                "/actuator/prometheus",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            ).permitAll()
            auth.requestMatchers("/api/**").authenticated()
            auth.anyRequest().permitAll()
        }
            .httpBasic { }

        return http.build()
    }
}
