package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.solution.SolutionRequest
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.SolutionStatus
import com.team.antiplagiat.models.User
import com.team.antiplagiat.service.SolutionService
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
import org.junit.jupiter.api.Assertions.assertThrows
import jakarta.servlet.http.HttpServletRequest
import com.team.antiplagiat.config.TokenPayload
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.time.LocalDateTime

@WebMvcTest(SolutionController::class)
class SolutionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var solutionService: SolutionService

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
        reset(solutionService)
    }

    @Test
    fun `get should return solution for owner`() {
        val user = User(id = 2L, login = "user", email = "user@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 1L, name = "Test Problem", description = "")
        val solution = Solution(
            id = 1L,
            user = user,
            problem = problem,
            language = "kotlin",
            filePath = "/test.kt",
            code = "fun main() {}",
            status = SolutionStatus.WAITING
        )

        whenever(solutionService.findById(1L)).thenReturn(solution)

        mockMvc.perform(get("/api/solutions/1").requestAttr("tokenPayload", TokenPayload(
            userId = 2L,
            login = "user",
            email = "user@example.com",
            role = "BASIC"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    fun `get should return 403 for non-owner non-admin`() {
        val user = User(id = 2L, login = "user", email = "user@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 1L, name = "Test Problem", description = "")
        val solution = Solution(
            id = 1L,
            user = user,
            problem = problem,
            language = "kotlin",
            filePath = "/test.kt",
            code = "fun main() {}",
            status = SolutionStatus.WAITING
        )

        whenever(solutionService.findById(1L)).thenReturn(solution)

        mockMvc.perform(get("/api/solutions/1").requestAttr("tokenPayload", TokenPayload(
            userId = 3L,
            login = "user3",
            email = "user3@example.com",
            role = "BASIC"
        )))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `delete should return 204 for owner`() {
        val user = User(id = 2L, login = "user", email = "user@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 1L, name = "Test Problem", description = "")
        val solution = Solution(
            id = 1L,
            user = user,
            problem = problem,
            language = "kotlin",
            filePath = "/test.kt",
            code = "",
            status = SolutionStatus.WAITING
        )

        whenever(solutionService.findById(1L)).thenReturn(solution)
        doNothing().whenever(solutionService).delete(1L)

        mockMvc.perform(delete("/api/solutions/1").requestAttr("tokenPayload", TokenPayload(
            userId = 2L,
            login = "user",
            email = "user@example.com",
            role = "BASIC"
        )))
            .andExpect(status().isNoContent)

        verify(solutionService, times(1)).delete(1L)
    }

    @Test
    fun `delete should return 403 for non-owner non-admin`() {
        val user = User(id = 2L, login = "user", email = "user@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 1L, name = "Test Problem", description = "")
        val solution = Solution(
            id = 1L,
            user = user,
            problem = problem,
            language = "kotlin",
            filePath = "/test.kt",
            code = "",
            status = SolutionStatus.WAITING
        )

        whenever(solutionService.findById(1L)).thenReturn(solution)

        mockMvc.perform(delete("/api/solutions/1").requestAttr("tokenPayload", TokenPayload(
            userId = 3L,
            login = "user3",
            email = "user3@example.com",
            role = "BASIC"
        )))
            .andExpect(status().isForbidden)
    }

}

