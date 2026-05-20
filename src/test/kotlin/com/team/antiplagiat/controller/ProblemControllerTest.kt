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
    fun `create should return 400 when validation fails - empty name`() {
        val request = ProblemRequest(
            name = "",
            description = "Valid description"
        )

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
            .andExpect(status().isBadRequest)
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
    fun `update should return 400 when validation fails - long name`() {
        val longName = "a".repeat(201)
        val request = ProblemRequest(
            name = longName,
            description = "Description"
        )

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
            .andExpect(status().isBadRequest)
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

    @Test
    fun `delete should return 403 when user is not admin`() {
        mockMvc.perform(
            delete("/api/problems/1")
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
    fun `update should return 403 when user is not admin`() {
        val request = ProblemRequest(
            name = "Updated",
            description = "Updated"
        )

        mockMvc.perform(
            put("/api/problems/1")
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
    fun `getAll should return 401 when token missing`() {
        mockMvc.perform(get("/api/problems"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get should return 401 when token missing`() {
        mockMvc.perform(get("/api/problems/1"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `create should return 401 when token missing`() {
        val request = ProblemRequest(
            name = "Test",
            description = "Test"
        )

        mockMvc.perform(
            post("/api/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `update should return 401 when token missing`() {
        val request = ProblemRequest(
            name = "Test",
            description = "Test"
        )

        mockMvc.perform(
            put("/api/problems/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `delete should return 401 when token missing`() {
        mockMvc.perform(delete("/api/problems/1"))
            .andExpect(status().isUnauthorized)
    }

}

