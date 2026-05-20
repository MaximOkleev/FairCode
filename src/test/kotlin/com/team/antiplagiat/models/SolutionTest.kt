package com.team.antiplagiat.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SolutionTest {

    @Test
    fun `solution expected default values`() {
        val solution = Solution()

        assertEquals(0L, solution.id)
        assertEquals(0L, solution.user.id)
        assertEquals(0L, solution.problem.id)
        assertEquals("", solution.language)
        assertEquals(SolutionStatus.WAITING, solution.status)
        assertEquals("", solution.filePath)
        assertNull(solution.code)
    }

    @Test
    fun `solution keep custom values`() {
        val user = User(id = 1L, login = "testuser", email = "test@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 2L, name = "Two Sum", description = "Find two numbers")
        val submittedAt = LocalDateTime.of(2026, 5, 1, 10, 30)
        val createdAt = LocalDateTime.of(2026, 5, 1, 10, 0)

        val solution = Solution(
            id = 5L,
            user = user,
            problem = problem,
            language = "Kotlin",
            status = SolutionStatus.COMPLETED,
            submittedAt = submittedAt,
            filePath = "/uploads/solution_1.kt",
            code = "fun main() { println(\"Hello\") }",
            createdAt = createdAt
        )

        assertEquals(5L, solution.id)
        assertEquals(user, solution.user)
        assertEquals(problem, solution.problem)
        assertEquals("Kotlin", solution.language)
        assertEquals(SolutionStatus.COMPLETED, solution.status)
        assertEquals(submittedAt, solution.submittedAt)
        assertEquals("/uploads/solution_1.kt", solution.filePath)
        assertEquals("fun main() { println(\"Hello\") }", solution.code)
        assertEquals(createdAt, solution.createdAt)
    }

    @Test
    fun `Solution status can be changed`() {
        val solution = Solution(status = SolutionStatus.WAITING)

        assertEquals(SolutionStatus.WAITING, solution.status)

        solution.status = SolutionStatus.PROCESSING
        assertEquals(SolutionStatus.PROCESSING, solution.status)

        solution.status = SolutionStatus.COMPLETED
        assertEquals(SolutionStatus.COMPLETED, solution.status)
    }

    @Test
    fun `Solution code can be optional`() {
        val solution1 = Solution(code = null)
        val solution2 = Solution(code = "some code")

        assertNull(solution1.code)
        assertEquals("some code", solution2.code)

        solution1.code = "updated code"
        assertEquals("updated code", solution1.code)
    }

    @Test
    fun `solution with different languages`() {
        val languages = listOf("Java", "Python", "C++", "JavaScript", "Kotlin")

        languages.forEach { language ->
            val solution = Solution(language = language)
            assertEquals(language, solution.language)
        }
    }
}

