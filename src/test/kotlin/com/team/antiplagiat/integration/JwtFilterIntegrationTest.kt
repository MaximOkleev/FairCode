package com.team.antiplagiat.integration

import com.team.antiplagiat.config.TokenService
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.UserRepository
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
class JwtFilterIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var rest: TestRestTemplate

    @Autowired
    lateinit var tokenService: TokenService

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `protected endpoint requires bearer token and accepts valid token`() {
        // create user in DB
        val user = User(login = "intuser", email = "int@example.com")
        val saved = userRepository.save(user)

        // generate token using real TokenService
        val token = tokenService.generateToken(saved)

        val url = "http://localhost:$port/api/users"

        // request without token -> should NOT be 200 (unauthenticated)
        val r1 = rest.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String::class.java)
        assertNotEquals(HttpStatus.OK, r1.statusCode)

        // request with Bearer token -> 200
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        val entity = HttpEntity<Any>(headers)
        val r2 = rest.exchange(url, HttpMethod.GET, entity, String::class.java)
        assertEquals(HttpStatus.OK, r2.statusCode)
        assertNotNull(r2.body)
    }
}

