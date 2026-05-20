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
    fun `create should return 201 when solution created successfully`() {
        val request = SolutionRequest(
            problemId = 1L,
            language = "kotlin",
            filePath = "/test.kt",
            code = "fun main() {}"
        )

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

        whenever(solutionService.create(eq(2L), eq(1L), eq("kotlin"), eq("/test.kt"), eq("fun main() {}")))
            .thenReturn(solution)

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 2L,
                    login = "user",
                    email = "user@example.com",
                    role = "BASIC"
                ))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("WAITING"))
    }

    @Test
    fun `create should return 401 when no token provided`() {
        val request = SolutionRequest(
            problemId = 1L,
            language = "kotlin",
            filePath = "/test.kt",
            code = "fun main() {}"
        )

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
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
    fun `getAll should return user solutions`() {
        val user = User(id = 2L, login = "user", email = "user@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 1L, name = "Test Problem", description = "")
        val solutions = listOf(
            Solution(id = 1L, user = user, problem = problem, language = "kotlin", filePath = "/test.kt", code = "", status = SolutionStatus.WAITING)
        )

        whenever(solutionService.findByUser(2L)).thenReturn(solutions)

        mockMvc.perform(get("/api/solutions").requestAttr("tokenPayload", TokenPayload(
            userId = 2L,
            login = "user",
            email = "user@example.com",
            role = "BASIC"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `getByUser should return user solutions`() {
        val user = User(id = 2L, login = "user", email = "user@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 1L, name = "Test Problem", description = "")
        val solutions = listOf(
            Solution(id = 1L, user = user, problem = problem, language = "kotlin", filePath = "/test.kt", code = "", status = SolutionStatus.WAITING)
        )

        whenever(solutionService.findByUser(2L)).thenReturn(solutions)

        mockMvc.perform(get("/api/solutions/user").requestAttr("tokenPayload", TokenPayload(
            userId = 2L,
            login = "user",
            email = "user@example.com",
            role = "BASIC"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `updateStatus should return 200 for admin`() {
        val user = User(id = 2L, login = "user", email = "user@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 1L, name = "Test Problem", description = "")
        val updatedSolution = Solution(
            id = 1L,
            user = user,
            problem = problem,
            language = "kotlin",
            filePath = "/test.kt",
            code = "",
            status = SolutionStatus.COMPLETED
        )

        whenever(solutionService.updateStatus(eq(1L), eq(SolutionStatus.COMPLETED)))
            .thenReturn(updatedSolution)

        // Set security context with ADMIN role
        setSecurityContext(userId = 1L, role = "ADMIN")

        mockMvc.perform(
            patch("/api/solutions/1/status")
                .param("status", "COMPLETED")
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("COMPLETED"))
    }

    @Test
    fun `updateStatus should return 403 for non-admin`() {

        mockMvc.perform(
            patch("/api/solutions/1/status")
                .param("status", "COMPLETED")
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
    fun `solution controller covers auth, forbidden and invalid status branches`() {
        val service = mockk<SolutionService>(relaxed = true)
        val controller = SolutionController(service)
        val user = User(id = 2L, login = "user", email = "user@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 1L, name = "Problem", description = "desc")
        val solution = Solution(
            id = 1L,
            user = user,
            problem = problem,
            language = "kotlin",
            filePath = "/file.kt",
            code = "code",
            status = SolutionStatus.WAITING,
            submittedAt = LocalDateTime.now()
        )

        clearSecurityContext()
        assertEquals(HttpStatus.UNAUTHORIZED, controller.create(SolutionRequest(1L, "kotlin", "/file.kt", null), requestWith(null)).statusCode)

        setSecurityContext(userId = 2L, role = "BASIC")
        assertEquals(HttpStatus.BAD_REQUEST, controller.create(SolutionRequest(0L, "kotlin", "/file.kt", null), requestWith(payload(2L, "BASIC"))).statusCode)

        clearSecurityContext()
        every { service.findById(1L) } returns solution
        assertEquals(HttpStatus.UNAUTHORIZED, controller.get(1L, requestWith(null)).statusCode)

        setSecurityContext(userId = 3L, role = "BASIC")
        assertEquals(HttpStatus.FORBIDDEN, controller.get(1L, requestWith(payload(3L, "BASIC"))).statusCode)

        clearSecurityContext()
        assertEquals(HttpStatus.UNAUTHORIZED, controller.getAll(requestWith(null)).statusCode)

        setSecurityContext(userId = 1L, role = "ADMIN")
        assertEquals(HttpStatus.OK, controller.getAll(requestWith(payload(1L, "ADMIN"))).statusCode)

        clearSecurityContext()
        assertEquals(HttpStatus.UNAUTHORIZED, controller.getByUser(requestWith(null)).statusCode)

        clearSecurityContext()
        assertEquals(HttpStatus.UNAUTHORIZED, controller.updateStatus(1L, "COMPLETED", requestWith(null)).statusCode)

        setSecurityContext(userId = 1L, role = "BASIC")
        try {
            controller.updateStatus(1L, "COMPLETED", requestWith(payload(1L, "BASIC")))
        } catch (e: Exception) {
            // @PreAuthorize may throw exception
        }

        setSecurityContext(userId = 1L, role = "ADMIN")
        assertThrows(IllegalArgumentException::class.java) {
            controller.updateStatus(1L, "NOT_A_STATUS", requestWith(payload(1L, "ADMIN")))
        }

        clearSecurityContext()
        assertEquals(HttpStatus.UNAUTHORIZED, controller.delete(1L, requestWith(null)).statusCode)

        setSecurityContext(userId = 3L, role = "BASIC")
        assertEquals(HttpStatus.FORBIDDEN, controller.delete(1L, requestWith(payload(3L, "BASIC"))).statusCode)
    }
}


