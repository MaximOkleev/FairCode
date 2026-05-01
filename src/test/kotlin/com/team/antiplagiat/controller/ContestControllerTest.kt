package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.ContestRequest
import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.User
import com.team.antiplagiat.service.ContestService
import com.team.antiplagiat.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(ContestController::class)
class ContestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var contestService: ContestService

    @MockitoBean
    private lateinit var userService: UserService

    @Test
    fun `create should return 201 when contest is created successfully`() {
        val startedAt = LocalDateTime.of(2026, 5, 1, 12, 0)
        val request = ContestRequest(
            name = "Spring Contest",
            adminId = 1L,
            startedAt = startedAt,
            duration = 3600
        )
        val admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        val contest = Contest(
            id = 100L,
            name = "Spring Contest",
            admin = admin,
            startedAt = startedAt,
            duration = 3600
        )

        whenever(userService.findById(1L)).thenReturn(admin)
        whenever(contestService.create(any())).thenReturn(contest)

        mockMvc.perform(
            post("/api/contests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.name").value("Spring Contest"))
            .andExpect(jsonPath("$.adminId").value(1))
            .andExpect(jsonPath("$.startedAt").exists())
            .andExpect(jsonPath("$.duration").value(3600))

        verify(contestService, times(1)).create(any())
    }

    @Test
    fun `create should return 400 when admin not found`() {
        val request = ContestRequest(
            name = "Spring Contest",
            adminId = 99L,
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = 3600
        )

        whenever(userService.findById(99L)).thenReturn(null)

        mockMvc.perform(
            post("/api/contests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(contestService, times(0)).create(any())
    }

    @Test
    fun `create should return 400 when service rejects contest`() {
        val startedAt = LocalDateTime.of(2026, 5, 1, 12, 0)
        val request = ContestRequest(
            name = "Spring Contest",
            adminId = 1L,
            startedAt = startedAt,
            duration = 3600
        )
        val admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)

        whenever(userService.findById(1L)).thenReturn(admin)
        whenever(contestService.create(any())).thenReturn(null)

        mockMvc.perform(
            post("/api/contests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `get should return 200 for existing contest`() {
        val startedAt = LocalDateTime.of(2026, 5, 1, 12, 0)
        val contest = Contest(
            id = 10L,
            name = "Contest 10",
            admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN),
            startedAt = startedAt,
            duration = 5400
        )

        whenever(contestService.findById(10L)).thenReturn(contest)

        mockMvc.perform(get("/api/contests/10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("Contest 10"))
            .andExpect(jsonPath("$.adminId").value(1))
            .andExpect(jsonPath("$.startedAt").exists())
            .andExpect(jsonPath("$.duration").value(5400))
    }

    @Test
    fun `get should return 404 for missing contest`() {
        whenever(contestService.findById(99L)).thenReturn(null)

        mockMvc.perform(get("/api/contests/99"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getAll should return mapped contests list`() {
        whenever(contestService.findAll()).thenReturn(
            listOf(
                Contest(
                    id = 1L,
                    name = "Contest 1",
                    admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN),
                    startedAt = LocalDateTime.of(2026, 5, 1, 10, 0),
                    duration = 3600
                ),
                Contest(
                    id = 2L,
                    name = "Contest 2",
                    admin = User(id = 2L, login = "judge", email = "judge@example.com", role = User.Role.ADMIN),
                    startedAt = LocalDateTime.of(2026, 5, 1, 11, 0),
                    duration = 7200
                )
            )
        )

        mockMvc.perform(get("/api/contests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Contest 1"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Contest 2"))
    }

    @Test
    fun `getByAdmin should return contests for a specific admin`() {
        whenever(contestService.findByAdmin(5L)).thenReturn(
            listOf(
                Contest(
                    id = 1L,
                    name = "Contest 1",
                    admin = User(id = 5L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN),
                    startedAt = LocalDateTime.of(2026, 5, 1, 10, 0),
                    duration = 3600
                ),
                Contest(
                    id = 2L,
                    name = "Contest 2",
                    admin = User(id = 5L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN),
                    startedAt = LocalDateTime.of(2026, 5, 1, 11, 0),
                    duration = 7200
                )
            )
        )

        mockMvc.perform(get("/api/contests/by-admin/5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].adminId").value(5))
            .andExpect(jsonPath("$[1].adminId").value(5))
    }

    @Test
    fun `update should return 200 when contest updated successfully`() {
        val updatedContest = Contest(
            id = 10L,
            name = "Updated name",
            admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN),
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = 5400
        )

        whenever(contestService.update(10L, "Updated name", 5400L)).thenReturn(updatedContest)

        mockMvc.perform(
            put("/api/contests/10")
                .param("name", "Updated name")
                .param("duration", "5400")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("Updated name"))
            .andExpect(jsonPath("$.duration").value(5400))
    }

    @Test
    fun `update should return 404 when contest is missing`() {
        whenever(contestService.update(99L, "Updated name", 5400L)).thenReturn(null)

        mockMvc.perform(
            put("/api/contests/99")
                .param("name", "Updated name")
                .param("duration", "5400")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `delete should return 204 and call service`() {
        doNothing().whenever(contestService).delete(10L)

        mockMvc.perform(delete("/api/contests/10"))
            .andExpect(status().isNoContent)

        verify(contestService, times(1)).delete(10L)
    }
}

