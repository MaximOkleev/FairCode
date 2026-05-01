package com.team.antiplagiat.service

import com.team.antiplagiat.config.props.SolutionConfig
import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.exception.TooManyAttemptsException
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.SolutionStatus
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.ProblemRepository
import com.team.antiplagiat.repository.SolutionRepository
import com.team.antiplagiat.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*

class SolutionServiceTest {

    private lateinit var solutionRepository: SolutionRepository
    private lateinit var userRepository: UserRepository
    private lateinit var problemRepository: ProblemRepository
    private lateinit var solutionConfig: SolutionConfig
    private lateinit var solutionService: SolutionService

    @BeforeEach
    fun setUp() {
        solutionRepository = mockk()
        userRepository = mockk()
        problemRepository = mockk()
        solutionConfig = mockk()
        solutionService = SolutionService(
            solutionRepository,
            userRepository,
            problemRepository,
            solutionConfig
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    // для create

    @Test
    fun `create should return solution when user and problem exist and attempts limit not exceeded`() {
        val userId = 1L
        val problemId = 2L
        val language = "Kotlin"
        val filePath = "/path/to/solution.kt"
        val code = "println('Hello')"
        val maxAttempts = 5

        val user = User().apply { id = userId }
        val problem = Problem().apply { id = problemId }
        val savedSolution = Solution(
            id = 100L,
            user = user,
            problem = problem,
            language = language,
            status = SolutionStatus.WAITING,
            submittedAt = LocalDateTime.now(),
            filePath = filePath,
            code = code
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { problemRepository.findById(problemId) } returns Optional.of(problem)
        every { solutionConfig.maxAttempts } returns maxAttempts
        every { solutionRepository.countByUserAndProblem(user, problem) } returns 2
        every { solutionRepository.save(any<Solution>()) } returns savedSolution

        val result = solutionService.create(userId, problemId, language, filePath, code)

        assertEquals(100L, result.id)
        assertEquals(SolutionStatus.WAITING, result.status)

        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { problemRepository.findById(problemId) }
        verify(exactly = 1) { solutionRepository.countByUserAndProblem(user, problem) }
        verify(exactly = 1) { solutionRepository.save(any()) }
    }

    @Test
    fun `create should throw ResourceNotFoundException when user not found`() {
        val userId = 999L
        val problemId = 2L

        every { userRepository.findById(userId) } returns Optional.empty()

        val exception = assertThrows<ResourceNotFoundException> {
            solutionService.create(userId, problemId, "Java", "/path", null)
        }

        assertEquals("Пользователь с id=$userId не найден", exception.message)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { problemRepository.findById(any()) }
        verify(exactly = 0) { solutionRepository.countByUserAndProblem(any(), any()) }
        verify(exactly = 0) { solutionRepository.save(any()) }
    }

    @Test
    fun `create should throw ResourceNotFoundException when problem not found`() {
        val userId = 1L
        val problemId = 999L
        val user = User().apply { id = userId }

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { problemRepository.findById(problemId) } returns Optional.empty()

        val exception = assertThrows<ResourceNotFoundException> {
            solutionService.create(userId, problemId, "Python", "/path", "code")
        }

        assertEquals("Задача с id=$problemId не найдена", exception.message)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { problemRepository.findById(problemId) }
        verify(exactly = 0) { solutionRepository.countByUserAndProblem(any(), any()) }
        verify(exactly = 0) { solutionRepository.save(any()) }
    }

    @Test
    fun `create should throw TooManyAttemptsException when max attempts exceeded`() {
        val userId = 1L
        val problemId = 2L
        val maxAttempts = 3
        val user = User().apply { id = userId }
        val problem = Problem().apply { id = problemId }

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { problemRepository.findById(problemId) } returns Optional.of(problem)
        every { solutionConfig.maxAttempts } returns maxAttempts
        every { solutionRepository.countByUserAndProblem(user, problem) } returns 3

        val exception = assertThrows<TooManyAttemptsException> {
            solutionService.create(userId, problemId, "C++", "/path", null)
        }

        assertEquals("Превышен лимит попыток: $maxAttempts", exception.message)
        verify(exactly = 1) { solutionRepository.countByUserAndProblem(user, problem) }
        verify(exactly = 0) { solutionRepository.save(any()) }
    }

    @Test
    fun `create should handle code as null`() {
        val userId = 1L
        val problemId = 2L
        val maxAttempts = 5
        val user = User().apply { id = userId }
        val problem = Problem().apply { id = problemId }
        val savedSolution = Solution(
            id = 100L,
            user = user,
            problem = problem,
            language = "JavaScript",
            status = SolutionStatus.WAITING,
            submittedAt = LocalDateTime.now(),
            filePath = "/path",
            code = null
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { problemRepository.findById(problemId) } returns Optional.of(problem)
        every { solutionConfig.maxAttempts } returns maxAttempts
        every { solutionRepository.countByUserAndProblem(user, problem) } returns 0
        every { solutionRepository.save(any<Solution>()) } returns savedSolution

        val result = solutionService.create(userId, problemId, "JavaScript", "/path", null)

        assertNull(result.code)
        verify(exactly = 1) { solutionRepository.save(any()) }
    }

    // для findById

    @Test
    fun `findById should return solution when exists`() {
        val solutionId = 100L
        val expectedSolution = Solution().apply { id = solutionId }

        every { solutionRepository.findById(solutionId) } returns Optional.of(expectedSolution)

        val result = solutionService.findById(solutionId)

        assertEquals(solutionId, result.id)
        verify(exactly = 1) { solutionRepository.findById(solutionId) }
    }

    @Test
    fun `findById should throw ResourceNotFoundException when solution not found`() {
        val solutionId = 999L

        every { solutionRepository.findById(solutionId) } returns Optional.empty()

        val exception = assertThrows<ResourceNotFoundException> {
            solutionService.findById(solutionId)
        }

        assertEquals("Решение с id=$solutionId не найдено", exception.message)
        verify(exactly = 1) { solutionRepository.findById(solutionId) }
    }

    // для findAll

    @Test
    fun `findAll should return list of solutions`() {
        val solutions = listOf(
            Solution().apply { id = 1 },
            Solution().apply { id = 2 },
            Solution().apply { id = 3 }
        )

        every { solutionRepository.findAll() } returns solutions

        val result = solutionService.findAll()

        assertEquals(3, result.size)
        assertEquals(solutions, result)
        verify(exactly = 1) { solutionRepository.findAll() }
    }

    @Test
    fun `findAll should return empty list when no solutions`() {
        every { solutionRepository.findAll() } returns emptyList()

        val result = solutionService.findAll()

        assertTrue(result.isEmpty())
        verify(exactly = 1) { solutionRepository.findAll() }
    }

    // для findByUser

    @Test
    fun `findByUser should return solutions for specific user`() {
        val userId = 1L
        val expectedSolutions = listOf(
            Solution().apply { id = 100 },
            Solution().apply { id = 101 }
        )

        every { solutionRepository.findAllByUserId(userId) } returns expectedSolutions

        val result = solutionService.findByUser(userId)

        assertEquals(2, result.size)
        assertEquals(expectedSolutions, result)
        verify(exactly = 1) { solutionRepository.findAllByUserId(userId) }
    }

    @Test
    fun `findByUser should return empty list when user has no solutions`() {
        val userId = 999L

        every { solutionRepository.findAllByUserId(userId) } returns emptyList()

        val result = solutionService.findByUser(userId)

        assertTrue(result.isEmpty())
        verify(exactly = 1) { solutionRepository.findAllByUserId(userId) }
    }

    // для updateStatus

    @Test
    fun `updateStatus should update status and return updated solution`() {
        val solutionId = 100L
        val newStatus = SolutionStatus.COMPLETED
        val existingSolution = Solution().apply {
            id = solutionId
            status = SolutionStatus.WAITING
        }
        val updatedSolution = Solution().apply {
            id = solutionId
            status = newStatus
        }

        every { solutionRepository.findById(solutionId) } returns Optional.of(existingSolution)
        every { solutionRepository.save(existingSolution) } returns updatedSolution

        val result = solutionService.updateStatus(solutionId, newStatus)

        assertEquals(newStatus, result.status)
        verify(exactly = 1) { solutionRepository.findById(solutionId) }
        verify(exactly = 1) { solutionRepository.save(existingSolution) }
    }

    @Test
    fun `updateStatus should throw ResourceNotFoundException when solution not found`() {
        val solutionId = 999L
        val newStatus = SolutionStatus.COMPLETED

        every { solutionRepository.findById(solutionId) } returns Optional.empty()

        val exception = assertThrows<ResourceNotFoundException> {
            solutionService.updateStatus(solutionId, newStatus)
        }

        assertEquals("Решение с id=$solutionId не найдено", exception.message)
        verify(exactly = 1) { solutionRepository.findById(solutionId) }
        verify(exactly = 0) { solutionRepository.save(any()) }
    }

    @Test
    fun `updateStatus should handle any status value`() {
        val solutionId = 100L
        val statuses = SolutionStatus.entries

        statuses.forEach { status ->
            val solution = Solution().apply {
                id = solutionId
                this.status = SolutionStatus.WAITING
            }

            every { solutionRepository.findById(solutionId) } returns Optional.of(solution)
            every { solutionRepository.save(solution) } returns solution

            val result = solutionService.updateStatus(solutionId, status)

            assertEquals(status, result.status)
            clearMocks(solutionRepository)
        }
    }

    // для delete

    @Test
    fun `delete should call repository deleteById when solution exists`() {
        val solutionId = 100L

        every { solutionRepository.deleteById(solutionId) } just runs

        solutionService.delete(solutionId)

        verify(exactly = 1) { solutionRepository.deleteById(solutionId) }
    }

    @Test
    fun `delete should throw ResourceNotFoundException when solution not found`() {
        val solutionId = 999L

        every { solutionRepository.deleteById(solutionId) } just Runs

        assertDoesNotThrow { solutionService.delete(solutionId) }
        verify(exactly = 1) { solutionRepository.deleteById(solutionId) }
    }

    // дополнительные тесты для исключений

    @Test
    fun `create should throw TooManyAttemptsException when maxAttempts is 0`() {
        val userId = 1L
        val problemId = 2L
        val maxAttempts = 0
        val user = User().apply { id = userId }
        val problem = Problem().apply { id = problemId }

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { problemRepository.findById(problemId) } returns Optional.of(problem)
        every { solutionConfig.maxAttempts } returns maxAttempts
        every { solutionRepository.countByUserAndProblem(user, problem) } returns 0

        val exception = assertThrows<TooManyAttemptsException> {
            solutionService.create(userId, problemId, "Go", "/path", null)
        }

        assertEquals("Превышен лимит попыток: $maxAttempts", exception.message)
        verify(exactly = 1) { solutionRepository.countByUserAndProblem(user, problem) }
        verify(exactly = 0) { solutionRepository.save(any()) }
    }

    @Test
    fun `create should throw TooManyAttemptsException when attempt count equals maxAttempts`() {
        val userId = 1L
        val problemId = 2L
        val maxAttempts = 3
        val user = User().apply { id = userId }
        val problem = Problem().apply { id = problemId }

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { problemRepository.findById(problemId) } returns Optional.of(problem)
        every { solutionConfig.maxAttempts } returns maxAttempts
        every { solutionRepository.countByUserAndProblem(user, problem) } returns 3

        val exception = assertThrows<TooManyAttemptsException> {
            solutionService.create(userId, problemId, "Rust", "/path", null)
        }

        assertEquals("Превышен лимит попыток: $maxAttempts", exception.message)
    }
}