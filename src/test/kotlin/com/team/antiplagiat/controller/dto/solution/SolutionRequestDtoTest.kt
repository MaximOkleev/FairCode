package com.team.antiplagiat.controller.dto.solution

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import com.team.antiplagiat.models.User
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.SolutionStatus
import java.time.LocalDateTime
import com.team.antiplagiat.controller.dto.SolutionRequest as RootSolutionRequest
import com.team.antiplagiat.controller.dto.SolutionResponse as RootSolutionResponse
import com.team.antiplagiat.controller.dto.toEntity as rootToEntity

class SolutionRequestDtoTest {

    @Test
    fun `should create SolutionRequest with all fields`() {
        val request = SolutionRequest(
            problemId = 1L,
            language = "kotlin",
            filePath = "/test.kt",
            code = "fun main() {}"
        )

        assertEquals(1L, request.problemId)
        assertEquals("kotlin", request.language)
        assertEquals("/test.kt", request.filePath)
        assertEquals("fun main() {}", request.code)
    }

    @Test
    fun `should handle null code`() {
        val request = SolutionRequest(
            problemId = 1L,
            language = "python",
            filePath = "/test.py",
            code = null
        )

        assertEquals(1L, request.problemId)
        assertEquals("python", request.language)
        assertEquals("/test.py", request.filePath)
        assertEquals(null, request.code)
    }

    @Test
    fun `should create SolutionRequest with different languages`() {
        val languages = listOf("kotlin", "java", "python", "cpp")

        for (lang in languages) {
            val request = SolutionRequest(
                problemId = 1L,
                language = lang,
                filePath = "/test.$lang",
                code = "code"
            )

            assertEquals(lang, request.language)
        }
    }

    @Test
    fun `solution dto mapping`() {
        val user = User(id = 3L, login = "u2", email = "u2@e.com", role = User.Role.BASIC)
        val problem = Problem(id = 4L, name = "P", description = "")
        val now = LocalDateTime.now()
        val sol = Solution(id = 11L, user = user, problem = problem, language = "kotlin", status = SolutionStatus.WAITING, submittedAt = now, filePath = "/f", code = "c")

        val resp = RootSolutionResponse.fromEntity(sol)
        assertEquals(11L, resp.id)
        assertEquals(3L, resp.userId)
        assertEquals(4L, resp.problemId)
        assertEquals(SolutionStatus.WAITING, resp.status)

        val req = RootSolutionRequest(userId = 3L, problemId = 4L, language = "kotlin", filePath = "/f", code = "c")
        val ent = req.rootToEntity(user, problem)
        assertEquals("kotlin", ent.language)
        assertEquals(user, ent.user)
        assertEquals(problem, ent.problem)
    }

    @Test
    fun `solution dto round trip`() {
        val user = User(id = 2L, login = "u2", email = "u2@example.com", role = User.Role.BASIC)
        val problem = Problem(id = 3L, name = "P", description = "D")
        val request = RootSolutionRequest(userId = user.id, problemId = problem.id, language = "kotlin", filePath = "/f.kt", code = "code")

        val entity = request.rootToEntity(user, problem)
        val response = RootSolutionResponse.fromEntity(
            Solution(
                id = 30L,
                user = user,
                problem = problem,
                language = entity.language,
                filePath = entity.filePath,
                code = entity.code,
                status = entity.status,
                submittedAt = LocalDateTime.now()
            )
        )

        assertEquals(user, entity.user)
        assertEquals(problem, entity.problem)
        assertEquals(SolutionStatus.WAITING, entity.status)
        assertEquals(30L, response.id)
        assertEquals(user.id, response.userId)
        assertEquals(problem.id, response.problemId)
        assertEquals(SolutionStatus.WAITING, response.status)
    }
}

