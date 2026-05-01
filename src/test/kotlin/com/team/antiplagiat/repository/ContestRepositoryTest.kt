package com.team.antiplagiat.repository

import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.LocalDateTime

@DataJpaTest
class ContestRepositoryTest {

    @Autowired
    private lateinit var contestRepository: ContestRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `findAllByAdminId returns contests for matching admin`() {
        val admin = persistAdmin("admin", "admin@example.com")
        val anotherAdmin = persistAdmin("judge", "judge@example.com")

        persistContest("Contest 1", admin, 3600)
        persistContest("Contest 2", admin, 5400)
        persistContest("Contest 3", anotherAdmin, 7200)

        val result = contestRepository.findAllByAdminId(admin.id)

        assertEquals(2, result.size)
        assertEquals("Contest 1", result[0].name)
        assertEquals("Contest 2", result[1].name)
    }

    @Test
    fun `save and findById preserve many to many problems`() {
        val admin = persistAdmin("admin", "admin@example.com")
        val problem = persistProblem()
        val contest = Contest(
            name = "Mapped Contest",
            admin = admin,
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = 3600,
            problems = mutableSetOf(problem)
        )

        val saved = contestRepository.saveAndFlush(contest)
        entityManager.clear()

        val loaded = contestRepository.findById(saved.id).orElse(null)

        assertNotNull(loaded)
        assertEquals("Mapped Contest", loaded?.name)
        assertEquals(1, loaded?.problems?.size)
        assertEquals(problem.id, loaded?.problems?.first()?.id)
    }

    @Test
    fun `deleteById removes contest`() {
        val admin = persistAdmin("admin", "admin@example.com")
        val contest = persistContest("Contest to delete", admin, 3600)

        contestRepository.deleteById(contest.id)
        entityManager.flush()
        entityManager.clear()

        val loaded = contestRepository.findById(contest.id)

        assertEquals(false, loaded.isPresent)
    }

    private fun persistAdmin(login: String, email: String): User {
        val user = User(login = login, email = email, role = User.Role.ADMIN)
        entityManager.persistAndFlush(user)
        return user
    }

    private fun persistProblem(): Problem {
        val problem = Problem(name = "Two Sum", description = "Find two numbers")
        entityManager.persistAndFlush(problem)
        return problem
    }

    private fun persistContest(name: String, admin: User, duration: Int): Contest {
        val contest = Contest(
            name = name,
            admin = admin,
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = duration
        )
        entityManager.persistAndFlush(contest)
        return contest
    }
}

