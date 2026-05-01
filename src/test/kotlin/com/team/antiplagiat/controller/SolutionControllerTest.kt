package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.SolutionRequest
import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.exception.TooManyAttemptsException
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
import java.time.LocalDateTime

@WebMvcTest(SolutionController::class)
class SolutionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var solutionService: SolutionService

    @BeforeEach
    fun setUp() {
        reset(solutionService)
    }

    @Test
    fun `create should return 201 when solution created successfully`() {
        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Kotlin",
            filePath = "/path/to/solution.kt",
            code = "println('Hello')"
        )

        val solution = Solution(
            id = 100L,
            user = createUser(1L),
            problem = createProblem(2L),
            language = "Kotlin",
            status = SolutionStatus.WAITING,
            submittedAt = LocalDateTime.now(),
            filePath = "/path/to/solution.kt",
            code = "println('Hello')"
        )

        whenever(solutionService.create(any(), any(), any(), any(), any())).thenReturn(solution)

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.problemId").value(2))
            .andExpect(jsonPath("$.language").value("Kotlin"))
            .andExpect(jsonPath("$.status").value("WAITING"))
            .andExpect(jsonPath("$.filePath").value("/path/to/solution.kt"))
            .andExpect(jsonPath("$.code").value("println('Hello')"))
            .andExpect(jsonPath("$.submittedAt").exists())
    }

    @Test
    fun `create should return 429 when max attempts exceeded`() {
        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Java",
            filePath = "/path",
            code = null
        )

        whenever(solutionService.create(eq(1L), eq(2L), eq("Java"), eq("/path"), eq(null)))
            .thenThrow(TooManyAttemptsException("Превышен лимит попыток: 5"))

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.message").value("Превышен лимит попыток: 5"))
    }

    @Test
    fun `create should return 404 when problem not found`() {
        val request = SolutionRequest(
            userId = 1L,
            problemId = 999L,
            language = "Java",
            filePath = "/path",
            code = null
        )

        whenever(solutionService.create(eq(1L), eq(999L), eq("Java"), eq("/path"), eq(null)))
            .thenThrow(ResourceNotFoundException("Задача с id=999 не найдена"))

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Задача с id=999 не найдена"))
    }

    @Test
    fun `create should return 404 when user not found`() {
        val request = SolutionRequest(
            userId = 999L,
            problemId = 2L,
            language = "Java",
            filePath = "/path",
            code = null
        )

        whenever(solutionService.create(eq(999L), eq(2L), eq("Java"), eq("/path"), eq(null)))
            .thenThrow(ResourceNotFoundException("Пользователь с id=999 не найден"))

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Пользователь с id=999 не найден"))
    }

    @Test
    fun `create should return 400 when request has missing fields`() {
        val invalidRequest = mapOf(
            "userId" to 1L,
            "problemId" to 2L
            // missing language, filePath
        )

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getById should return 200 when solution exists`() {
        val solution = createSolution(
            id = 100L,
            userId = 1L,
            problemId = 2L,
            language = "Kotlin",
            status = SolutionStatus.COMPLETED,
            filePath = "/path/to/solution.kt",
            code = "println('Hello')"
        )

        whenever(solutionService.findById(100L)).thenReturn(solution)

        mockMvc.perform(get("/api/solutions/100"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.problemId").value(2))
            .andExpect(jsonPath("$.language").value("Kotlin"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.filePath").value("/path/to/solution.kt"))
            .andExpect(jsonPath("$.code").value("println('Hello')"))
    }

    @Test
    fun `getById should return 404 when solution not found`() {
        whenever(solutionService.findById(999L))
            .thenThrow(ResourceNotFoundException("Решение с id=999 не найдено"))

        mockMvc.perform(get("/api/solutions/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Решение с id=999 не найдено"))
    }

    @Test
    fun `getAll should return list of solutions`() {
        val solutions = listOf(
            createSolution(id = 1L, userId = 1L, problemId = 1L, language = "Kotlin"),
            createSolution(id = 2L, userId = 2L, problemId = 1L, language = "Java"),
            createSolution(id = 3L, userId = 1L, problemId = 2L, language = "Python")
        )

        whenever(solutionService.findAll()).thenReturn(solutions)

        mockMvc.perform(get("/api/solutions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].language").value("Java"))
            .andExpect(jsonPath("$[2].status").value("WAITING"))
    }

    @Test
    fun `getAll should return empty list when no solutions`() {
        whenever(solutionService.findAll()).thenReturn(emptyList())

        mockMvc.perform(get("/api/solutions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `getByUser should return solutions for specific user`() {
        val solutions = listOf(
            createSolution(id = 1L, userId = 1L, problemId = 1L),
            createSolution(id = 3L, userId = 1L, problemId = 2L)
        )

        whenever(solutionService.findByUser(1L)).thenReturn(solutions)

        mockMvc.perform(get("/api/solutions/user/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].userId").value(1))
            .andExpect(jsonPath("$[1].userId").value(1))
    }

    @Test
    fun `getByUser should return empty list when user has no solutions`() {
        whenever(solutionService.findByUser(999L)).thenReturn(emptyList())

        mockMvc.perform(get("/api/solutions/user/999"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `updateStatus should return 200 when status updated successfully`() {
        val updatedSolution = createSolution(
            id = 100L,
            userId = 1L,
            problemId = 2L,
            status = SolutionStatus.COMPLETED
        )

        whenever(solutionService.updateStatus(eq(100L), eq(SolutionStatus.COMPLETED))).thenReturn(updatedSolution)

        mockMvc.perform(
            patch("/api/solutions/100/status")
                .param("status", "COMPLETED")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.id").value(100))
    }

    @Test
    fun `updateStatus should return 200 with lowercase status`() {
        val updatedSolution = createSolution(
            id = 100L,
            userId = 1L,
            problemId = 2L,
            status = SolutionStatus.PROCESSING
        )

        whenever(solutionService.updateStatus(eq(100L), eq(SolutionStatus.PROCESSING))).thenReturn(updatedSolution)

        mockMvc.perform(
            patch("/api/solutions/100/status")
                .param("status", "processing")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PROCESSING"))
    }

    @Test
    fun `updateStatus should return 400 when status is invalid`() {
        mockMvc.perform(
            patch("/api/solutions/100/status")
                .param("status", "INVALID_STATUS")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `updateStatus should return 400 when status is missing`() {
        mockMvc.perform(
            patch("/api/solutions/100/status")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `updateStatus should return 404 when solution not found`() {
        whenever(solutionService.updateStatus(eq(999L), eq(SolutionStatus.COMPLETED)))
            .thenThrow(ResourceNotFoundException("Решение с id=999 не найдено"))

        mockMvc.perform(
            patch("/api/solutions/999/status")
                .param("status", "COMPLETED")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Решение с id=999 не найдено"))
    }

    @Test
    fun `delete should return 204 when solution exists`() {
        doNothing().whenever(solutionService).delete(100L)

        mockMvc.perform(delete("/api/solutions/100"))
            .andExpect(status().isNoContent)

        verify(solutionService, times(1)).delete(100L)
    }

    @Test
    fun `delete should return 404 when solution does not exist`() {
        doThrow(ResourceNotFoundException("Решение с id=999 не найдено"))
            .whenever(solutionService).delete(999L)

        mockMvc.perform(delete("/api/solutions/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Решение с id=999 не найдено"))

        verify(solutionService, times(1)).delete(999L)
    }

    // вспомогательные методы

    private fun createUser(id: Long) = User(
        id = id,
        login = "user$id",
        email = "user$id@example.com",
        role = User.Role.BASIC
    )

    private fun createProblem(id: Long) = Problem(
        id = id,
        name = "Problem $id",
        description = "Description for problem $id"
    )

    private fun createSolution(
        id: Long,
        userId: Long = 1L,
        problemId: Long = 1L,
        language: String = "Kotlin",
        status: SolutionStatus = SolutionStatus.WAITING,
        filePath: String = "/path/to/solution.kt",
        code: String? = "println('Hello World')"
    ): Solution = Solution(
        id = id,
        user = createUser(userId),
        problem = createProblem(problemId),
        language = language,
        status = status,
        submittedAt = LocalDateTime.now(),
        filePath = filePath,
        code = code
    )
}