package com.team.antiplagiat.controller.dto.contest

import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ContestRequestDtoTest {

    @Test
    fun `should convert ContestRequest to Contest entity`() {
        val now = LocalDateTime.now()
        val request = ContestRequest(
            name = "Test Contest",
            startedAt = now,
            duration = 120
        )

        val admin = User(
            id = 1L,
            login = "admin",
            email = "admin@example.com",
            role = User.Role.ADMIN
        )

        val entity = request.toEntity(admin)

        assertEquals("Test Contest", entity.name)
        assertEquals(now, entity.startedAt)
        assertEquals(120, entity.duration)
        assertEquals(admin, entity.admin)
    }

    @Test
    fun `should handle different durations`() {
        val now = LocalDateTime.now()
        val request = ContestRequest(
            name = "Contest 1",
            startedAt = now,
            duration = 60
        )

        val admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        val entity = request.toEntity(admin)

        assertEquals(60, entity.duration)
    }

    @Test
    fun `contest dto mapping`() {
        val admin = User(id = 10L, login = "admin", email = "a@e.com", role = User.Role.ADMIN)
        val contest = com.team.antiplagiat.models.Contest(
            id = 5L,
            name = "C",
            admin = admin,
            startedAt = LocalDateTime.now(),
            duration = 60
        )

        val resp = ContestResponse.fromEntity(contest)
        assertEquals(5L, resp.id)
        assertEquals("C", resp.name)
        assertEquals(10L, resp.adminId)

        val req = ContestRequest(name = "C2", startedAt = LocalDateTime.now(), duration = 30)
        val ent = req.toEntity(admin)
        assertEquals("C2", ent.name)
        assertEquals(admin, ent.admin)
    }

    @Test
    fun `contest dto round trip`() {
        val admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        val now = LocalDateTime.now()
        val request = ContestRequest(name = "Contest", startedAt = now, duration = 120)

        val entity = request.toEntity(admin)
        val response = ContestResponse.fromEntity(
            com.team.antiplagiat.models.Contest(
                id = 10L,
                name = entity.name,
                admin = admin,
                startedAt = entity.startedAt,
                duration = entity.duration
            )
        )

        assertEquals("Contest", entity.name)
        assertEquals(admin, entity.admin)
        assertEquals(10L, response.id)
        assertEquals("Contest", response.name)
        assertEquals(admin.id, response.adminId)
        assertEquals(120, response.duration)
    }
}

