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
    fun `register login and access protected resource`() {
        val email = "e2euser@example.com"
        val password = "password123"

        // register
        val regReq = RegisterRequest(email = email, password = password)
        val regRespRaw = rest.postForEntity(url("/api/register"), regReq, String::class.java)
        assertEquals(HttpStatus.CREATED, regRespRaw.statusCode)
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        val regBody = mapper.readTree(regRespRaw.body)
        val tokenFromRegister = regBody.get("token")?.asText()
        assertNotNull(tokenFromRegister)

        // login (login is email prefix)
        val login = email.substringBefore("@")
        val loginReq = LoginRequest(login = login, password = password)
        val loginRespRaw = rest.postForEntity(url("/api/auth/login"), loginReq, String::class.java)
        assertEquals(HttpStatus.OK, loginRespRaw.statusCode)
        val loginBody = com.fasterxml.jackson.databind.ObjectMapper().readTree(loginRespRaw.body)
        val tokenFromLogin = loginBody.get("token")?.asText()
        assertNotNull(tokenFromLogin)

        // access protected resource with token
        val headers = HttpHeaders()
        headers.setBearerAuth(tokenFromLogin!!)
        val entity = HttpEntity<Any>(headers)
        val r = rest.exchange(url("/api/users"), HttpMethod.GET, entity, String::class.java)
        assertEquals(HttpStatus.OK, r.statusCode)
    }
}

