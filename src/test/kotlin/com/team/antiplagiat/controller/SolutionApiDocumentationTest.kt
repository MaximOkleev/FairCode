package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.SolutionRequest
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.User
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.repository.SolutionRepository
import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.repository.ProblemRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Доступные endpoints:
 * - POST   /api/solutions              → Создать новое решение
 * - GET    /api/solutions              → Получить все решения
 * - GET    /api/solutions/{id}         → Получить решение по ID
 * - GET    /api/solutions/user/{userId} → Получить решения пользователя
 * - PATCH  /api/solutions/{id}/status  → Обновить статус
 * - DELETE /api/solutions/{id}         → Удалить решение
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("API Documentation & Example Requests")
class SolutionApiDocumentationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var solutionRepository: SolutionRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    private var testSolutionId: Long = 0
    private var testUserId: Long = 1
    private var testProblemId: Long = 1
    private lateinit var testUser: User
    private lateinit var testProblem: Problem

    @BeforeEach
    fun setUp() {
        solutionRepository.deleteAll()
        userRepository.deleteAll()
        problemRepository.deleteAll()

        testUser = User(
            login = "testuser",
            email = "test@example.com",
            passwordHash = "\$2a\$10\$fIZ0W0dNFtI5QZbJvY1I6OZzg4IJs6Uo2dLcXrOh/ZLmZrSv5fYO2",
            role = User.Role.BASIC
        )
        testUser = userRepository.save(testUser)
        testUserId = testUser.id!!

        testProblem = Problem(
            name = "Test Problem",
            description = "Test Description"
        )
        testProblem = problemRepository.save(testProblem)
        testProblemId = testProblem.id!!

        val solution = Solution(
            user = testUser,
            problem = testProblem,
            language = "kotlin",
            filePath = "/home/user/solution.kt",
            code = """
                fun main() {
                    println("Hello, World!")
                }
            """.trimIndent(),
            status = "COMPLETED"
        )
        val saved = solutionRepository.save(solution)
        testSolutionId = saved.id!!
    }

    @Test
    @DisplayName("POST /api/solutions - Успешное создание решения")
    fun `should create solution successfully`() {
        val request = SolutionRequest(
            userId = testUserId,
            problemId = testProblemId,
            language = "python",
            filePath = "/home/student/project1.py",
            code = """
                def hello():
                    print("Hello, Python!")
            """.trimIndent()
        )

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.language").value("python"))
            .andExpect(jsonPath("$.status").exists())
            .andDo { result ->
                println("\nRESPONSE (POST /api/solutions):")
                println(result.response.contentAsString)
            }
    }

    @Test
    @DisplayName("POST /api/solutions - Ошибка: отсутствует userId (валидация)")
    fun `should return 400 when userId is missing`() {
        val request = mapOf(
            "problemId" to testProblemId,
            "language" to "java",
            "filePath" to "/home/user/solution.java"
        )

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andDo { result ->
                println("\nVALIDATION ERROR RESPONSE:")
                println(result.response.contentAsString)
            }
    }

    @Test
    @DisplayName("POST /api/solutions - Ошибка: пустой язык программирования")
    fun `should return 400 when language is blank`() {
        val request = SolutionRequest(
            userId = testUserId,
            problemId = testProblemId,
            language = "",  // Пусто!
            filePath = "/home/user/solution.kt",
            code = "fun main() {}"
        )

        mockMvc.perform(
            post("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.language").exists())
    }


    @Test
    @DisplayName("GET /api/solutions - Получить все решения")
    fun `should return all solutions`() {
        mockMvc.perform(
            get("/api/solutions")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(testSolutionId.toInt()))
            .andExpect(jsonPath("$[0].userId").value(testUserId.toInt()))
            .andDo { result ->
                println("\nRESPONSE (GET /api/solutions):")
                println(result.response.contentAsString)
            }
    }


    @Test
    @DisplayName("GET /api/solutions/{id} - Получить решение по ID")
    fun `should return solution by id`() {
        mockMvc.perform(
            get("/api/solutions/$testSolutionId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.problemId").exists())
            .andExpect(jsonPath("$.language").value("kotlin"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.filePath").value("/home/user/solution.kt"))
            .andDo { result ->
                println("\nRESPONSE (GET /api/solutions/$testSolutionId):")
                println(result.response.contentAsString)
            }
    }

    @Test
    @DisplayName("GET /api/solutions/{id} - Ошибка: решение не найдено")
    fun `should return 404 when solution not found`() {
        mockMvc.perform(
            get("/api/solutions/99999")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andDo { result ->
                println("\n404 NOT FOUND RESPONSE:")
                println(result.response.contentAsString)
            }
    }

    @Test
    @DisplayName("GET /api/solutions/user/{userId} - Получить решения пользователя")
    fun `should return solutions by user id`() {
        mockMvc.perform(
            get("/api/solutions/user/$testUserId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].userId").value(testUserId.toInt()))
            .andDo { result ->
                println("\nRESPONSE (GET /api/solutions/user/$testUserId):")
                println(result.response.contentAsString)
            }
    }

    @Test
    @DisplayName("GET /api/solutions/user/{userId} - Пользователь без решений")
    fun `should return empty list for user without solutions`() {
        mockMvc.perform(
            get("/api/solutions/user/99999")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @DisplayName("PATCH /api/solutions/{id}/status - Обновить статус на CHECKING")
    fun `should update solution status to CHECKING`() {
        mockMvc.perform(
            patch("/api/solutions/$testSolutionId/status")
                .param("status", "CHECKING")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testSolutionId.toInt()))
            .andExpect(jsonPath("$.status").value("CHECKING"))
            .andDo { result ->
                println("\nRESPONSE (PATCH /api/solutions/$testSolutionId/status?status=CHECKING):")
                println(result.response.contentAsString)
            }
    }

    @Test
    @DisplayName("PATCH /api/solutions/{id}/status - Обновить статус на PLAGIARISM_DETECTED")
    fun `should update solution status to PLAGIARISM_DETECTED`() {
        mockMvc.perform(
            patch("/api/solutions/$testSolutionId/status")
                .param("status", "PLAGIARISM_DETECTED")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PLAGIARISM_DETECTED"))
            .andDo { result ->
                println("\nRESPONSE - Detected plagiarism:")
                println(result.response.contentAsString)
            }
    }

    @Test
    @DisplayName("PATCH /api/solutions/{id}/status - Ошибка: решение не найдено")
    fun `should return 404 when updating status for non-existent solution`() {
        mockMvc.perform(
            patch("/api/solutions/99999/status")
                .param("status", "COMPLETED")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("DELETE /api/solutions/{id} - Успешное удаление решения")
    fun `should delete solution successfully`() {
        mockMvc.perform(
            get("/api/solutions/$testSolutionId")
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            delete("/api/solutions/$testSolutionId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)
            .andDo { result ->
                println("\nDELETE Response (no content):")
                println("Status: ${result.response.status}")
            }

        mockMvc.perform(
            get("/api/solutions/$testSolutionId")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("DELETE /api/solutions/{id} - DELETE всегда возвращает 204")
    fun `should return 204 when deleting non-existent solution`() {
        mockMvc.perform(
            delete("/api/solutions/99999")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)
            .andDo { result ->
                println("\nDELETE 204 Response (No Content - idempotent):")
                println("Status: ${result.response.status}")
            }
    }

    /**
     * 💡 ПРИМЕРЫ CURL КОМАНД для тестирования API:
     *
     * СОЗДАТЬ РЕШЕНИЕ:
     *    curl -X POST http://localhost:8080/api/solutions \
     *         -H "Content-Type: application/json" \
     *         -d '{
     *           "userId": 1,
     *           "problemId": 1,
     *           "language": "python",
     *           "filePath": "/home/user/solution.py",
     *           "code": "print(\"Hello\")"
     *         }'
     *
     * ПОЛУЧИТЬ ВСЕ РЕШЕНИЯ:
     *    curl http://localhost:8080/api/solutions
     *
     * ПОЛУЧИТЬ РЕШЕНИЕ ПО ID:
     *    curl http://localhost:8080/api/solutions/1
     *
     * ПОЛУЧИТЬ РЕШЕНИЯ ПОЛЬЗОВАТЕЛЯ:
     *    curl http://localhost:8080/api/solutions/user/1
     *
     * ОБНОВИТЬ СТАТУС:
     *    curl -X PATCH http://localhost:8080/api/solutions/1/status?status=CHECKING
     *
     * УДАЛИТЬ РЕШЕНИЕ:
     *    curl -X DELETE http://localhost:8080/api/solutions/1
     *
     * ОТКРИТЬ SWAGGER UI:
     *    http://localhost:8080/swagger-ui.html
     *
     * ОТКРИТЬ OpenAPI JSON:
     *    http://localhost:8080/v3/api-docs
     */
}

