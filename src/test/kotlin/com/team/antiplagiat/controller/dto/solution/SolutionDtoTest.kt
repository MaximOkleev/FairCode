package com.team.antiplagiat.controller.dto.solution

import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.SolutionStatus
import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class SolutionDtoTest {

    @Test
    fun `SolutionRequest should have all required fields`() {
        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Kotlin",
            filePath = "/path/to/solution.kt",
            code = "println('Hello')"
        )

        assertEquals(1L, request.userId)
        assertEquals(2L, request.problemId)
        assertEquals("Kotlin", request.language)
        assertEquals("/path/to/solution.kt", request.filePath)
        assertEquals("println('Hello')", request.code)
    }

    @Test
    fun `SolutionRequest toEntity should create Solution with WAITING status`() {
        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Java",
            filePath = "/path/to/solution.java",
            code = "public class Main {}"
        )

        val user = User(id = 1L, login = "user1", email = "user1@test.com", role = User.Role.BASIC)
        val problem = Problem(id = 2L, name = "Problem", description = "Description")

        val solution = request.toEntity(user, problem)

        assertEquals(user, solution.user)
        assertEquals(problem, solution.problem)
        assertEquals("Java", solution.language)
        assertEquals("/path/to/solution.java", solution.filePath)
        assertEquals("public class Main {}", solution.code)
        assertEquals(SolutionStatus.WAITING, solution.status)
    }

    @Test
    fun `SolutionRequest toEntity should handle null code`() {
        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Python",
            filePath = "/path/to/solution.py",
            code = null
        )

        val user = User(id = 1L, login = "user1", email = "user1@test.com", role = User.Role.BASIC)
        val problem = Problem(id = 2L, name = "Problem", description = "Description")

        val solution = request.toEntity(user, problem)

        assertNull(solution.code)
    }

    @Test
    fun `SolutionResponse fromEntity should convert Solution correctly`() {
        val user = User(id = 1L, login = "user1", email = "user1@test.com", role = User.Role.BASIC)
        val problem = Problem(id = 2L, name = "Problem", description = "Description")
        val now = LocalDateTime.now()
        val solution = Solution(
            id = 100L,
            user = user,
            problem = problem,
            language = "Kotlin",
            status = SolutionStatus.COMPLETED,
            submittedAt = now,
            filePath = "/path/to/solution.kt",
            code = "println('Hello')"
        )

        val response = SolutionResponse.fromEntity(solution)

        assertEquals(100L, response.id)
        assertEquals(1L, response.userId)
        assertEquals(2L, response.problemId)
        assertEquals("Kotlin", response.language)
        assertEquals(SolutionStatus.COMPLETED, response.status)
        assertEquals(now, response.submittedAt)
        assertEquals("/path/to/solution.kt", response.filePath)
        assertEquals("println('Hello')", response.code)
    }

    @Test
    fun `SolutionResponse should handle all status types`() {
        val user = User(id = 1L, login = "user1", email = "user1@test.com", role = User.Role.BASIC)
        val problem = Problem(id = 2L, name = "Problem", description = "Description")
        val now = LocalDateTime.now()

        val statuses = listOf(
            SolutionStatus.WAITING,
            SolutionStatus.PROCESSING,
            SolutionStatus.COMPLETED,
            SolutionStatus.FAILED,
            SolutionStatus.CANCELLED
        )

        statuses.forEach { status ->
            val solution = Solution(
                id = 100L,
                user = user,
                problem = problem,
                language = "Kotlin",
                status = status,
                submittedAt = now,
                filePath = "/path/to/solution.kt",
                code = "code"
            )

            val response = SolutionResponse.fromEntity(solution)
            assertEquals(status, response.status)
        }
    }
}

