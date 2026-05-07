package com.team.antiplagiat.controller.dto.contest

import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.User
import com.team.antiplagiat.controller.dto.ContestRequest as RootContestRequest
import com.team.antiplagiat.controller.dto.ContestResponse as RootContestResponse
import com.team.antiplagiat.controller.dto.toEntity as toRootEntity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class ContestDtoTest {

    @Test
    fun `ContestRequest toEntity should create Contest correctly`() {
        val now = LocalDateTime.now()
        val request = ContestRequest(
            name = "Spring Contest",
            adminId = 1L,
            startedAt = now,
            duration = 120
        )

        val admin = User(id = 1L, login = "admin", email = "admin@test.com", role = User.Role.ADMIN)
        val contest = request.toEntity(admin)

        assertEquals("Spring Contest", contest.name)
        assertEquals(admin, contest.admin)
        assertEquals(now, contest.startedAt)
        assertEquals(120, contest.duration)
        assertEquals(0L, contest.id)
    }

    @Test
    fun `ContestResponse fromEntity should convert Contest correctly`() {
        val admin = User(id = 1L, login = "admin", email = "admin@test.com", role = User.Role.ADMIN)
        val now = LocalDateTime.now()
        val contest = Contest(
            id = 10L,
            name = "Algorithm Contest",
            admin = admin,
            startedAt = now,
            duration = 180
        )

        val response = ContestResponse.fromEntity(contest)

        assertEquals(10L, response.id)
        assertEquals("Algorithm Contest", response.name)
        assertEquals(1L, response.adminId)
        assertEquals(now, response.startedAt)
        assertEquals(180, response.duration)
    }

    @Test
    fun `ContestRequest and Response should handle different durations`() {
        val admin = User(id = 2L, login = "judge", email = "judge@test.com", role = User.Role.ADMIN)
        val now = LocalDateTime.now()

        val contests = listOf(
            ContestRequest("Short Contest", 2L, now, 60),
            ContestRequest("Medium Contest", 2L, now, 120),
            ContestRequest("Long Contest", 2L, now, 240)
        )

        contests.forEach { request ->
            val contest = request.toEntity(admin)
            val response = ContestResponse.fromEntity(contest)
            assertEquals(request.duration, response.duration)
        }
    }

    @Test
    fun `ContestResponse should properly extract adminId from admin entity`() {
        val admin = User(id = 99L, login = "test_admin", email = "test@test.com", role = User.Role.ADMIN)
        val now = LocalDateTime.now()
        val contest = Contest(
            id = 1L,
            name = "Test",
            admin = admin,
            startedAt = now,
            duration = 60
        )

        val response = ContestResponse.fromEntity(contest)

        assertEquals(99L, response.adminId)
        assertEquals(admin.id, response.adminId)
    }

    @Test
    fun `root ContestRequest toEntity should create Contest correctly`() {
        val now = LocalDateTime.of(2026, 5, 7, 12, 0)
        val request = RootContestRequest(name = "Root Contest", adminId = 10L, startedAt = now, duration = 90)
        val admin = User(id = 10L, login = "root_admin", email = "root@admin.test", role = User.Role.ADMIN)

        val contest = request.toRootEntity(admin)

        assertEquals("Root Contest", contest.name)
        assertEquals(admin, contest.admin)
        assertEquals(now, contest.startedAt)
        assertEquals(90, contest.duration)
        assertEquals(0L, contest.id)
    }

    @Test
    fun `root ContestResponse fromEntity should convert Contest correctly`() {
        val admin = User(id = 11L, login = "adm11", email = "adm11@test.com", role = User.Role.ADMIN)
        val now = LocalDateTime.of(2026, 5, 7, 13, 30)
        val contest = Contest(id = 77L, name = "Root Response Contest", admin = admin, startedAt = now, duration = 180)

        val response = RootContestResponse.fromEntity(contest)

        assertEquals(77L, response.id)
        assertEquals("Root Response Contest", response.name)
        assertEquals(11L, response.adminId)
        assertEquals(now, response.startedAt)
        assertEquals(180, response.duration)
    }
}

