package com.team.antiplagiat.config

import com.team.antiplagiat.repository.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.assertj.core.api.Assertions.assertThat

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should have ExceptionHandler as ControllerAdvice`() {
        assertThat(applicationContext.containsBean("restExceptionHandler")).isTrue
        val handler = applicationContext.getBean("restExceptionHandler")
        assertThat(handler).isNotNull
    }

    @Test
    fun `should have DatabaseUserDetailsService bean created`() {
        assertThat(applicationContext.containsBean("databaseUserDetailsService")).isTrue
        val service = applicationContext.getBean("databaseUserDetailsService", DatabaseUserDetailsService::class.java)
        assertThat(service).isNotNull
    }

    @Test
    fun `should handle validation errors gracefully`() {
        mockMvc.post("/api/solutions") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }
    }

    @Test
    fun `should have UserRepository available`() {
        assertThat(userRepository).isNotNull
        assertThat(applicationContext.containsBean("userRepository")).isTrue
    }

    @Test
    fun `security support beans are available and filter chain is created`() {
        val hasSecurityFilterChain = applicationContext.containsBean("securityFilterChain")
        val hasPasswordEncoder = applicationContext.containsBean("passwordEncoder")
        assertThat(hasSecurityFilterChain).isTrue
        assertThat(hasPasswordEncoder).isTrue
    }
}

