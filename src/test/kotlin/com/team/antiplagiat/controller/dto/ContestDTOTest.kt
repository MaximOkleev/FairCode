package com.team.antiplagiat.controller.dto

import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ContestDTOTest {

    @Test
    fun `fromEntity maps contest fields to response`() {
        val startedAt = LocalDateTime.of(2026, 5, 1, 10, 30)
        val contest = mockk<Contest>()
        every { contest.id } returns 42L
        every { contest.name } returns "Spring Contest"
        every { contest.admin } returns User(id = 7L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        every { contest.startedAt } returns startedAt
        every { contest.duration } returns 7200

        val response = ContestResponse.fromEntity(contest)

        assertEquals(42L, response.id)
        assertEquals("Spring Contest", response.name)
        assertEquals(7L, response.adminId)
        assertEquals(startedAt, response.startedAt)
        assertEquals(7200, response.duration)
    }

    @Test
    fun `toEntity maps request fields to contest`() {
        val startedAt = LocalDateTime.of(2026, 5, 1, 12, 0)
        val request = ContestRequest(
            name = "April Challenge",
            adminId = 11L,
            startedAt = startedAt,
            duration = 3600
        )
        val admin = User(id = 11L, login = "judge", email = "judge@example.com", role = User.Role.ADMIN)

        val contest = request.toEntity(admin)

        assertEquals(0L, contest.id)
        assertEquals("April Challenge", contest.name)
        assertEquals(admin, contest.admin)
        assertEquals(startedAt, contest.startedAt)
        assertEquals(3600, contest.duration)
    }
}

