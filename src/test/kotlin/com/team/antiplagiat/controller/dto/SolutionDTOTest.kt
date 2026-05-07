package com.team.antiplagiat.controller.dto

import com.team.antiplagiat.controller.dto.solution.SolutionRequest
import com.team.antiplagiat.controller.dto.solution.SolutionResponse
import com.team.antiplagiat.controller.dto.solution.toEntity
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.SolutionStatus
import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SolutionDTOTest {

    @Test
    fun `SolutionRequest can be created with all fields`() {
        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Kotlin",
            filePath = "/path/to/file.kt",
            code = "fun main() {}"
        )

        assertEquals(1L, request.userId)
        assertEquals(2L, request.problemId)
        assertEquals("Kotlin", request.language)
        assertEquals("/path/to/file.kt", request.filePath)
        assertEquals("fun main() {}", request.code)
    }

    @Test
    fun `SolutionRequest with null code`() {
        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Python",
            filePath = "/path/solution.py",
            code = null
        )

        assertNull(request.code)
        assertEquals("Python", request.language)
    }

    @Test
    fun `SolutionRequest toEntity creates solution with correct fields`() {
        val user = User(id = 1L, login = "user1", email = "user1@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 2L, name = "Two Sum", description = "Find two numbers")
        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "Java",
            filePath = "/uploads/solution.java",
            code = "class Solution {}"
        )

        val solution = request.toEntity(user, problem)

        assertEquals(user, solution.user)
        assertEquals(problem, solution.problem)
        assertEquals("Java", solution.language)
        assertEquals("/uploads/solution.java", solution.filePath)
        assertEquals("class Solution {}", solution.code)
        assertEquals(SolutionStatus.WAITING, solution.status)
    }

    @Test
    fun `SolutionRequest toEntity with null code`() {
        val user = User(id = 1L, login = "user1", email = "user1@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 2L, name = "Two Sum", description = "Find two")
        val request = SolutionRequest(
            userId = 1L,
            problemId = 2L,
            language = "C++",
            filePath = "/solution.cpp",
            code = null
        )

        val solution = request.toEntity(user, problem)

        assertNull(solution.code)
        assertEquals(SolutionStatus.WAITING, solution.status)
    }

    @Test
    fun `SolutionResponse can be created with all fields`() {
        val submittedAt = LocalDateTime.of(2026, 5, 1, 10, 0)
        val response = SolutionResponse(
            id = 10L,
            userId = 1L,
            problemId = 2L,
            language = "Rust",
            status = SolutionStatus.COMPLETED,
            submittedAt = submittedAt,
            filePath = "/solution.rs",
            code = "fn main() {}"
        )

        assertEquals(10L, response.id)
        assertEquals(1L, response.userId)
        assertEquals(2L, response.problemId)
        assertEquals("Rust", response.language)
        assertEquals(SolutionStatus.COMPLETED, response.status)
        assertEquals(submittedAt, response.submittedAt)
        assertEquals("/solution.rs", response.filePath)
        assertEquals("fn main() {}", response.code)
    }

    @Test
    fun `SolutionResponse fromEntity extracts correct user and problem IDs`() {
        val user = User(id = 5L, login = "user5", email = "user5@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 7L, name = "Three Sum", description = "Find three")
        val submittedAt = LocalDateTime.of(2026, 5, 1, 14, 30)
        val solution = Solution(
            id = 100L,
            user = user,
            problem = problem,
            language = "Go",
            status = SolutionStatus.PROCESSING,
            submittedAt = submittedAt,
            filePath = "/upload.go",
            code = "package main"
        )

        val response = SolutionResponse.fromEntity(solution)

        assertEquals(100L, response.id)
        assertEquals(5L, response.userId)
        assertEquals(7L, response.problemId)
        assertEquals("Go", response.language)
        assertEquals(SolutionStatus.PROCESSING, response.status)
        assertEquals("/upload.go", response.filePath)
        assertEquals("package main", response.code)
    }

    @Test
    fun `SolutionResponse fromEntity with null code`() {
        val user = User(id = 1L, login = "user1", email = "user1@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 2L, name = "Problem", description = "Test")
        val solution = Solution(
            id = 50L,
            user = user,
            problem = problem,
            language = "Node.js",
            status = SolutionStatus.FAILED,
            submittedAt = LocalDateTime.now(),
            filePath = "/file.js",
            code = null
        )

        val response = SolutionResponse.fromEntity(solution)

        assertNull(response.code)
        assertEquals(50L, response.id)
        assertEquals(SolutionStatus.FAILED, response.status)
    }

    @Test
    fun `SolutionResponse fromEntity preserves all status types`() {
        val user = User(id = 1L, login = "user1", email = "user1@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 2L, name = "Problem", description = "Test")

        listOf(SolutionStatus.WAITING, SolutionStatus.PROCESSING, SolutionStatus.COMPLETED,
               SolutionStatus.FAILED, SolutionStatus.CANCELLED).forEach { status ->
            val solution = Solution(
                id = 1L,
                user = user,
                problem = problem,
                language = "Kotlin",
                status = status,
                submittedAt = LocalDateTime.now(),
                filePath = "/file.kt",
                code = null
            )

            val response = SolutionResponse.fromEntity(solution)

            assertEquals(status, response.status)
        }
    }

    @Test
    fun `SolutionResponse with different languages`() {
        val languages = listOf("Java", "Python", "C++", "JavaScript", "Go", "Rust", "C#")

        languages.forEach { language ->
            val response = SolutionResponse(
                id = 1L,
                userId = 1L,
                problemId = 1L,
                language = language,
                status = SolutionStatus.WAITING,
                submittedAt = LocalDateTime.now(),
                filePath = "/file",
                code = null
            )

            assertEquals(language, response.language)
        }
    }

    @Test
    fun `SolutionRequest equality`() {
        val request1 = SolutionRequest(1L, 2L, "Kotlin", "/path", "code")
        val request2 = SolutionRequest(1L, 2L, "Kotlin", "/path", "code")

        assertEquals(request1, request2)
    }

    @Test
    fun `SolutionResponse equality`() {
        val now = LocalDateTime.now()
        val response1 = SolutionResponse(1L, 1L, 2L, "Kotlin", SolutionStatus.WAITING, now, "/path", "code")
        val response2 = SolutionResponse(1L, 1L, 2L, "Kotlin", SolutionStatus.WAITING, now, "/path", "code")

        assertEquals(response1, response2)
    }
}

