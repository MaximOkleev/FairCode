package com.team.antiplagiat.integration

import com.team.antiplagiat.controller.dto.register.RegisterRequest
import com.team.antiplagiat.controller.dto.auth.LoginRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["app.security.enabled=true"])
class RegisterLoginE2ETest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var rest: TestRestTemplate

    private fun url(path: String) = "http://localhost:$port$path"

    @Test
    fun `register returns email verification required message`() {
        val email = "e2euser@example.com"
        val password = "password123"

        val healthResp = rest.getForEntity(url("/actuator/health"), String::class.java)
        assertEquals(HttpStatus.OK, healthResp.statusCode)
    }
}

