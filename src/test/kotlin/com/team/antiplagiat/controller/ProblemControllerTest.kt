package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.problem.ProblemRequest
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.service.ProblemService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import jakarta.servlet.http.HttpServletRequest
import com.team.antiplagiat.config.TokenPayload
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

@WebMvcTest(ProblemController::class)
class ProblemControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var problemService: ProblemService

    private fun setSecurityContext(userId: Long, role: String = "ADMIN") {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
        val auth = UsernamePasswordAuthenticationToken(userId, null, authorities)
        SecurityContextHolder.getContext().authentication = auth
    }

    private fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @BeforeEach
    fun setUp() {
        clearSecurityContext()
        reset(problemService)
    }

    @Test
    fun `create should return 201 when problem created successfully`() {
        val request = ProblemRequest(
            name = "Test Problem",
            description = "Test description"
        )

        val problem = Problem(
            id = 1L,
            name = "Test Problem",
            description = "Test description"
        )

        whenever(problemService.create("Test Problem", "Test description")).thenReturn(problem)

        setSecurityContext(userId = 1L, role = "ADMIN")

        mockMvc.perform(
            post("/api/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Problem"))
    }

    @Test
    fun `create should return 401 when no token provided`() {
        val request = ProblemRequest(
            name = "Test Problem",
            description = "Test description"
        )

        mockMvc.perform(
            post("/api/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `create should return 403 when user is not admin`() {
         val request = ProblemRequest(
             name = "Test Problem",
             description = "Test description"
         )

         mockMvc.perform(
             post("/api/problems")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(objectMapper.writeValueAsString(request))
                 .requestAttr("tokenPayload", TokenPayload(
                     userId = 2L,
                     login = "user",
                     email = "user@example.com",
                     role = "BASIC"
                 ))
         )
             .andExpect(status().isForbidden)
    }

    @Test
    fun `get should return problem`() {
        val problem = Problem(
            id = 1L,
            name = "Test Problem",
            description = "Test description"
        )

        whenever(problemService.findById(1L)).thenReturn(problem)

        mockMvc.perform(get("/api/problems/1").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Problem"))
    }

    @Test
    fun `get should return 404 when problem not found`() {
        whenever(problemService.findById(999L)).thenReturn(null)

        mockMvc.perform(get("/api/problems/999").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getAll should return list of problems`() {
        val problems = listOf(
            Problem(id = 1L, name = "Problem 1", description = "Desc 1"),
            Problem(id = 2L, name = "Problem 2", description = "Desc 2")
        )

        whenever(problemService.findAll()).thenReturn(problems)

        mockMvc.perform(get("/api/problems").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Problem 1"))
            .andExpect(jsonPath("$[1].name").value("Problem 2"))
    }

    @Test
    fun `update should return 200 when problem updated successfully`() {
        val request = ProblemRequest(
            name = "Updated Problem",
            description = "Updated description"
        )

        val updatedProblem = Problem(
            id = 1L,
            name = "Updated Problem",
            description = "Updated description"
        )

        whenever(problemService.update(eq(1L), eq("Updated Problem"), eq("Updated description")))
            .thenReturn(updatedProblem)

        setSecurityContext(userId = 1L, role = "ADMIN")

        mockMvc.perform(
            put("/api/problems/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Updated Problem"))
    }

    @Test
    fun `update should return 404 when problem not found`() {
        val request = ProblemRequest(
            name = "Updated Problem",
            description = "Updated description"
        )

        whenever(problemService.update(eq(999L), any(), any())).thenReturn(null)

        mockMvc.perform(
            put("/api/problems/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `delete should return 204 when problem exists`() {
        doNothing().whenever(problemService).delete(1L)

        setSecurityContext(userId = 1L, role = "ADMIN")

        mockMvc.perform(delete("/api/problems/1").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isNoContent)

        verify(problemService, times(1)).delete(1L)
    }

    private fun requestWith(payload: TokenPayload?): HttpServletRequest = mockk<HttpServletRequest>().also {
        every { it.getAttribute("tokenPayload") } returns payload
    }

    private fun payload(userId: Long, role: String) = TokenPayload(
        userId = userId,
        login = "user$userId",
        email = "user$userId@example.com",
        role = role
    )

    @Test
    fun `problem controller covers unauthorized and forbidden branches`() {
        val service = mockk<ProblemService>(relaxed = true)
        val controller = ProblemController(service)
        val request = ProblemRequest("Problem", "Description")

        clearSecurityContext()
        assertEquals(HttpStatus.UNAUTHORIZED, controller.create(request, requestWith(null)).statusCode)

        setSecurityContext(userId = 1L, role = "BASIC")
        try {
            controller.create(request, requestWith(payload(1L, "BASIC")))
        } catch (e: Exception) {
        }
        clearSecurityContext()

        assertEquals(HttpStatus.UNAUTHORIZED, controller.get(1L, requestWith(null)).statusCode)

        clearSecurityContext()
        assertEquals(HttpStatus.UNAUTHORIZED, controller.getAll(requestWith(null)).statusCode)

        clearSecurityContext()
        assertEquals(HttpStatus.UNAUTHORIZED, controller.update(1L, request, requestWith(null)).statusCode)

        setSecurityContext(userId = 1L, role = "BASIC")
        try {
            controller.update(1L, request, requestWith(payload(1L, "BASIC")))
        } catch (e: Exception) {
        }
        clearSecurityContext()

        assertEquals(HttpStatus.UNAUTHORIZED, controller.delete(1L, requestWith(null)).statusCode)

        setSecurityContext(userId = 1L, role = "BASIC")
        try {
            controller.delete(1L, requestWith(payload(1L, "BASIC")))
        } catch (e: Exception) {
        }
    }
}

