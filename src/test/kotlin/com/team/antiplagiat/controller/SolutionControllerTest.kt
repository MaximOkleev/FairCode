package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.SolutionRequest
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.service.SolutionService
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


//для POST /api/solutions

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
            status = "waiting",
            submittedAt = LocalDateTime.now(),
            filePath = "/path/to/solution.kt",
            code = "println('Hello')"
        )


        whenever(solutionService.create(1L, 2L, "Kotlin", "/path/to/solution.kt", "println('Hello')"))
            .thenReturn(solution)


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
            .andExpect(jsonPath("$.status").value("waiting"))
    }

    @Test
    fun `create should return 400 when user not found`() {

        val request = SolutionRequest(
            userId = 999L,
            problemId = 2L,
            language = "Java",
            filePath = "/path",
            code = null
        )


        whenever(solutionService.create(eq(999L), any(), any(), any(), any())).thenReturn(null)


        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create should return 400 when problem not found`() {

        val request = SolutionRequest(
            userId = 1L,
            problemId = 999L,
            language = "Java",
            filePath = "/path",
            code = null
        )


        whenever(solutionService.create(any(), eq(999L), any(), any(), any())).thenReturn(null)


        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create should return 400 when max attempts exceeded`() {

        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Java",
            filePath = "/path",
            code = null
        )


        whenever(solutionService.create(eq(1L), eq(2L), any(), any(), any())).thenReturn(null)


        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create should handle request with code null`() {

        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Python",
            filePath = "/path/to/solution.py",
            code = null
        )

        val solution = createSolution(
            id = 100L,
            userId = 1L,
            problemId = 2L,
            language = "Python",
            filePath = "/path/to/solution.py",
            code = null
        )


        whenever(solutionService.create(eq(1L), eq(2L), eq("Python"), eq("/path/to/solution.py"), isNull()))
            .thenReturn(solution)


        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.code").doesNotExist())
    }

// для GET /api/solutions/{id}

    @Test
    fun `getById should return 200 when solution exists`() {

        val solution = createSolution(
            id = 100L,
            userId = 1L,
            problemId = 2L,
            language = "Kotlin",
            status = "completed"
        )

        whenever(solutionService.findById(100L)).thenReturn(solution)


        mockMvc.perform(get("/api/solutions/100"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.status").value("completed"))
    }

    @Test
    fun `getById should return 404 when solution not found`() {

        whenever(solutionService.findById(999L)).thenReturn(null)


        mockMvc.perform(get("/api/solutions/999"))
            .andExpect(status().isNotFound)
    }

// Для GET /api/solutions

    @Test
    fun `getAll should return list of solutions`() {

        val solutions = listOf(
            createSolution(id = 1L, userId = 1L, problemId = 1L),
            createSolution(id = 2L, userId = 2L, problemId = 1L),
            createSolution(id = 3L, userId = 1L, problemId = 2L)
        )

        whenever(solutionService.findAll()).thenReturn(solutions)


        mockMvc.perform(get("/api/solutions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
    }

    @Test
    fun `getAll should return empty list when no solutions`() {

        whenever(solutionService.findAll()).thenReturn(emptyList())


        mockMvc.perform(get("/api/solutions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

//для GET /api/solutions/user/{userId}

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
    }

    @Test
    fun `getByUser should return empty list when user has no solutions`() {

        whenever(solutionService.findByUser(999L)).thenReturn(emptyList())


        mockMvc.perform(get("/api/solutions/user/999"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

// для PATCH /api/solutions/{id}/status

    @Test
    fun `updateStatus should return 200 when status updated successfully`() {

        val updatedSolution = createSolution(
            id = 100L,
            userId = 1L,
            problemId = 2L,
            status = "completed"
        )

        whenever(solutionService.updateStatus(100L, "completed")).thenReturn(updatedSolution)


        mockMvc.perform(patch("/api/solutions/100/status?status=completed"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("completed"))
    }

    @Test
    fun `updateStatus should return 404 when solution not found`() {

        whenever(solutionService.updateStatus(999L, "completed")).thenReturn(null)


        mockMvc.perform(patch("/api/solutions/999/status?status=completed"))
            .andExpect(status().isNotFound)
    }

// для DELETE /api/solutions/{id}

    @Test
    fun `delete should return 204`() {

        doNothing().whenever(solutionService).delete(100L)


        mockMvc.perform(delete("/api/solutions/100"))
            .andExpect(status().isNoContent)

        verify(solutionService, times(1)).delete(100L)
    }

    @Test
    fun `delete should work even when solution does not exist`() {

        doNothing().whenever(solutionService).delete(999L)


        mockMvc.perform(delete("/api/solutions/999"))
            .andExpect(status().isNoContent)

        verify(solutionService, times(1)).delete(999L)
    }



    private fun createUser(id: Long) = com.team.antiplagiat.models.User().apply {
        this.id = id
    }

    private fun createProblem(id: Long) = com.team.antiplagiat.models.Problem().apply {
        this.id = id
    }

    private fun createSolution(
        id: Long,
        userId: Long = 1L,
        problemId: Long = 1L,
        language: String = "Kotlin",
        status: String = "waiting",
        filePath: String = "/path",
        code: String? = "code"
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