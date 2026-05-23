package com.team.antiplagiat.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.ApplicationContext
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.beans.factory.ObjectProvider
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


private val logger = KotlinLogging.logger {}

@Configuration
@EnableWebSecurity
@Suppress("unused")
class SecurityConfig(private val env: Environment) {

    // JwtAuthenticationFilter will be created explicitly below when TokenService is present

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jsonAuthenticationEntryPoint(objectMapper: ObjectMapper): AuthenticationEntryPoint = JsonAuthenticationEntryPoint(objectMapper)

    @Bean
    fun jsonAccessDeniedHandler(objectMapper: ObjectMapper): AccessDeniedHandler = JsonAccessDeniedHandler(objectMapper)

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = false
            maxAge = 3600
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

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
    fun securityFilterChain(http: HttpSecurity, authEntryPointProvider: ObjectProvider<AuthenticationEntryPoint>, accessDeniedProvider: ObjectProvider<AccessDeniedHandler>): SecurityFilterChain {
        val enabled = env.getProperty("app.security.enabled", Boolean::class.java, false)
        http.cors { }
        http.csrf { it.disable() }
        if (!enabled) {
            http.authorizeHttpRequests { it.anyRequest().permitAll() }
            return http.build()
        }

        http.authorizeHttpRequests { auth ->
            auth.requestMatchers(
                "/actuator/health",
                "/actuator/prometheus",
                "/api/register",
                "/api/auth/login",
                "/api/auth/verify-email",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            ).permitAll()
            auth.requestMatchers("/api/**").authenticated()
            auth.anyRequest().permitAll()
        }
        http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        http.exceptionHandling { eh ->
            authEntryPointProvider.ifAvailable { eh.authenticationEntryPoint(it) }
            accessDeniedProvider.ifAvailable { eh.accessDeniedHandler(it) }
        }

        val ctx: ApplicationContext? = http.getSharedObject(ApplicationContext::class.java)
        ctx?.getBeanProvider(TokenService::class.java)?.ifAvailable { tokenService ->
            http.addFilterBefore(JwtAuthenticationFilter(tokenService), UsernamePasswordAuthenticationFilter::class.java)
        }

        return http.build()
    }
}
