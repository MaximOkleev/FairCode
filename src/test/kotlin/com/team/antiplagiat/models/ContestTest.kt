package com.team.antiplagiat.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ContestTest {

    @Test
    fun `contest has expected default values`() {
        val contest = Contest()

        assertEquals(0L, contest.id)
        assertEquals("", contest.name)
        assertEquals(0L, contest.admin.id)
        assertEquals(User.Role.BASIC, contest.admin.role)
        assertTrue(contest.problems.isEmpty())
        assertEquals(0, contest.duration)
        assertFalse(contest.startedAt.isAfter(LocalDateTime.now().plusSeconds(1)))
    }

    @Test
    fun `contest keeps custom values`() {
        val startedAt = LocalDateTime.of(2026, 5, 1, 9, 15)
        val admin = User(id = 5L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        val problem = Problem(id = 3L, name = "Two Sum", description = "Find two numbers")
        val contest = Contest(
            id = 12L,
            name = "Contest Finals",
            admin = admin,
            startedAt = startedAt,
            duration = 5400,
            problems = mutableSetOf(problem)
        )

        assertEquals(12L, contest.id)
        assertEquals("Contest Finals", contest.name)
        assertEquals(admin, contest.admin)
        assertEquals(startedAt, contest.startedAt)
        assertEquals(5400, contest.duration)
        assertEquals(1, contest.problems.size)
        assertEquals(problem, contest.problems.first())
    }
}

