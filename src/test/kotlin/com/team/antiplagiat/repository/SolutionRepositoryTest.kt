package com.team.antiplagiat.repository

import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.SolutionStatus
import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.LocalDateTime

@DataJpaTest
class SolutionRepositoryTest {

    @Autowired
    private lateinit var solutionRepository: SolutionRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `findAllByUserId returns solutions for matching user`() {
        val user = persistUser("user1", "user1@example.com")
        val other = persistUser("user2", "user2@example.com")
        val problem = persistProblem("Two Sum")

        persistSolution(user, problem, "Kotlin", SolutionStatus.WAITING)
        persistSolution(user, problem, "Python", SolutionStatus.COMPLETED)
        persistSolution(other, problem, "Java", SolutionStatus.WAITING)

        val result = solutionRepository.findAllByUserId(user.id)
        assertEquals(2, result.size)
    }

    @Test
    fun `findAllByUserId returns empty list if no solutions`() {
        val user = persistUser("empty", "empty@example.com")
        assertEquals(0, solutionRepository.findAllByUserId(user.id).size)
    }

    @Test
    fun `countByUserAndProblem counts correctly`() {
        val user = persistUser("user1", "user1@example.com")
        val p1 = persistProblem("Two Sum")
        val p2 = persistProblem("Three Sum")

        persistSolution(user, p1, "Kotlin", SolutionStatus.WAITING)
        persistSolution(user, p1, "Python", SolutionStatus.COMPLETED)
        persistSolution(user, p2, "Java", SolutionStatus.FAILED)

        assertEquals(2L, solutionRepository.countByUserAndProblem(user, p1))
        assertEquals(1L, solutionRepository.countByUserAndProblem(user, p2))
    }

    @Test
    fun `countByUserAndProblem returns zero when no solutions`() {
        val user = persistUser("user1", "user1@example.com")
        val problem = persistProblem("Two Sum")
        assertEquals(0L, solutionRepository.countByUserAndProblem(user, problem))
    }

    @Test
    fun `save and findById preserve solution data`() {
        val user = persistUser("user1", "user1@example.com")
        val problem = persistProblem("Two Sum")

        val solution = Solution(
            user = user, problem = problem, language = "Kotlin",
            status = SolutionStatus.COMPLETED,
            submittedAt = LocalDateTime.of(2026, 5, 1, 14, 30),
            filePath = "/uploads/sol.kt", code = "fun main() {}"
        )

        val saved = solutionRepository.saveAndFlush(solution)
        entityManager.clear()
        val loaded = solutionRepository.findById(saved.id).orElse(null)

        assertEquals("Kotlin", loaded?.language)
        assertEquals(SolutionStatus.COMPLETED, loaded?.status)
        assertEquals(user.id, loaded?.user?.id)
    }

    @Test
    fun `deleteById removes solution`() {
        val user = persistUser("user1", "user1@example.com")
        val problem = persistProblem("Two Sum")
        val solution = persistSolution(user, problem, "Kotlin", SolutionStatus.WAITING)

        solutionRepository.deleteById(solution.id)
        entityManager.flush()
        entityManager.clear()

        assertEquals(false, solutionRepository.findById(solution.id).isPresent)
    }

    @Test
    fun `update solution status`() {
        val user = persistUser("user1", "user1@example.com")
        val problem = persistProblem("Two Sum")
        val solution = persistSolution(user, problem, "Kotlin", SolutionStatus.WAITING)

        solution.status = SolutionStatus.COMPLETED
        solutionRepository.saveAndFlush(solution)
        entityManager.clear()

        val loaded = solutionRepository.findById(solution.id).orElse(null)
        assertEquals(SolutionStatus.COMPLETED, loaded?.status)
    }

    @Test
    fun `findAll returns all solutions with correct relationships`() {
        val user1 = persistUser("user1", "user1@example.com")
        val user2 = persistUser("user2", "user2@example.com")
        val problem = persistProblem("Problem 1")

        persistSolution(user1, problem, "Kotlin", SolutionStatus.WAITING)
        persistSolution(user2, problem, "Python", SolutionStatus.COMPLETED)

        val allSolutions = solutionRepository.findAll()

        assertEquals(2, allSolutions.size)
        assertTrue(allSolutions.any { it.language == "Kotlin" && it.user.id == user1.id })
        assertTrue(allSolutions.any { it.language == "Python" && it.user.id == user2.id })
    }

    @Test
    fun `countByUserAndProblem handles different user problem combinations`() {
        val user1 = persistUser("user1", "user1@example.com")
        val user2 = persistUser("user2", "user2@example.com")
        val problem1 = persistProblem("Problem 1")
        val problem2 = persistProblem("Problem 2")

        persistSolution(user1, problem1, "Kotlin", SolutionStatus.WAITING)
        persistSolution(user1, problem2, "Python", SolutionStatus.COMPLETED)
        persistSolution(user2, problem1, "Java", SolutionStatus.FAILED)

        assertEquals(1L, solutionRepository.countByUserAndProblem(user1, problem1))
        assertEquals(1L, solutionRepository.countByUserAndProblem(user1, problem2))
        assertEquals(1L, solutionRepository.countByUserAndProblem(user2, problem1))
        assertEquals(0L, solutionRepository.countByUserAndProblem(user2, problem2))
    }

    @Test
    fun `solution with different statuses`() {
        val user = persistUser("user1", "user1@example.com")
        val problem = persistProblem("Problem 1")

        persistSolution(user, problem, "Kotlin", SolutionStatus.WAITING)
        persistSolution(user, problem, "Python", SolutionStatus.PROCESSING)
        persistSolution(user, problem, "Java", SolutionStatus.COMPLETED)
        persistSolution(user, problem, "C++", SolutionStatus.FAILED)
        persistSolution(user, problem, "Rust", SolutionStatus.CANCELLED)

        val solutions = solutionRepository.findAllByUserId(user.id)
        assertEquals(5, solutions.size)
        assertTrue(solutions.any { it.status == SolutionStatus.WAITING })
        assertTrue(solutions.any { it.status == SolutionStatus.COMPLETED })
        assertTrue(solutions.any { it.status == SolutionStatus.FAILED })
        assertTrue(solutions.any { it.status == SolutionStatus.CANCELLED })
        assertTrue(solutions.any { it.status == SolutionStatus.PROCESSING })
    }

    private fun persistUser(login: String, email: String): User {
        val suffix = System.nanoTime().toString().takeLast(6)
        val user = User(
            login = "$login-$suffix",
            email = email.replace("@", "+$suffix@"),
            role = User.Role.BASIC
        )
        entityManager.persistAndFlush(user)
        return user
    }

    private fun persistProblem(name: String): Problem {
        val problem = Problem(name = name, description = "Description")
        entityManager.persistAndFlush(problem)
        return problem
    }

    private fun persistSolution(
        user: User,
        problem: Problem,
        language: String,
        status: SolutionStatus
    ): Solution {
        val solution = Solution(
            user = user,
            problem = problem,
            language = language,
            status = status,
            submittedAt = LocalDateTime.of(2026, 5, 1, 10, 0),
            filePath = "/uploads/${System.nanoTime()}"
        )
        entityManager.persistAndFlush(solution)
        return solution
    }
}

