package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.contest.ContestRequest
import com.team.antiplagiat.controller.dto.contest.ContestResponse
import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.User
import com.team.antiplagiat.service.ContestService
import com.team.antiplagiat.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
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

    @BeforeEach
    fun setUp() {
        reset(contestService, userService)
    }

    @Test
    fun `create should return 201 when contest created successfully`() {
        val now = LocalDateTime.now()
        val request = ContestRequest(
            name = "Test Contest",
            adminId = 1L,
            startedAt = now,
            duration = 120
        )

        val admin = User(
            id = 1L,
            login = "admin",
            email = "admin@example.com",
            role = User.Role.ADMIN
        )

        val contest = Contest(
            id = 1L,
            name = "Test Contest",
            admin = admin,
            startedAt = now,
            duration = 120
        )

        whenever(userService.findById(1L)).thenReturn(admin)
        whenever(contestService.create(any())).thenReturn(contest)

        mockMvc.perform(
            post("/api/contests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Contest"))
            .andExpect(jsonPath("$.adminId").value(1))
            .andExpect(jsonPath("$.duration").value(120))
    }

    @Test
    fun `create should return 400 when admin not found`() {
        val now = LocalDateTime.now()
        val request = ContestRequest(
            name = "Test Contest",
            adminId = 999L,
            startedAt = now,
            duration = 120
        )

        whenever(userService.findById(999L)).thenReturn(null)

        mockMvc.perform(
            post("/api/contests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getById should return 200 when contest exists`() {
        val admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        val now = LocalDateTime.now()
        val contest = Contest(
            id = 1L,
            name = "Test Contest",
            admin = admin,
            startedAt = now,
            duration = 120
        )

        whenever(contestService.findById(1L)).thenReturn(contest)

        mockMvc.perform(get("/api/contests/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Contest"))
            .andExpect(jsonPath("$.adminId").value(1))
    }

    @Test
    fun `getById should return 404 when contest not found`() {
        whenever(contestService.findById(999L)).thenReturn(null)

        mockMvc.perform(get("/api/contests/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getAll should return list of contests`() {
        val admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        val now = LocalDateTime.now()
        val contests = listOf(
            Contest(id = 1L, name = "Contest 1", admin = admin, startedAt = now, duration = 120),
            Contest(id = 2L, name = "Contest 2", admin = admin, startedAt = now, duration = 180)
        )

        whenever(contestService.findAll()).thenReturn(contests)

        mockMvc.perform(get("/api/contests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Contest 1"))
            .andExpect(jsonPath("$[1].duration").value(180))
    }

    @Test
    fun `getAll should return empty list when no contests`() {
        whenever(contestService.findAll()).thenReturn(emptyList())

        mockMvc.perform(get("/api/contests"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `getByAdmin should return contests for specific admin`() {
        val admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        val now = LocalDateTime.now()
        val contests = listOf(
            Contest(id = 1L, name = "Contest 1", admin = admin, startedAt = now, duration = 120),
            Contest(id = 2L, name = "Contest 2", admin = admin, startedAt = now, duration = 180)
        )

        whenever(contestService.findByAdmin(1L)).thenReturn(contests)

        mockMvc.perform(get("/api/contests/by-admin/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].adminId").value(1))
            .andExpect(jsonPath("$[1].adminId").value(1))
    }

    @Test
    fun `getByAdmin should return empty list when admin has no contests`() {
        whenever(contestService.findByAdmin(999L)).thenReturn(emptyList())

        mockMvc.perform(get("/api/contests/by-admin/999"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `update should return 200 when contest updated successfully`() {
        val admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        val now = LocalDateTime.now()
        val updatedContest = Contest(
            id = 1L,
            name = "Updated Contest",
            admin = admin,
            startedAt = now,
            duration = 150
        )

        whenever(contestService.update(eq(1L), eq("Updated Contest"), eq(150L))).thenReturn(updatedContest)

        mockMvc.perform(
            put("/api/contests/1")
                .param("name", "Updated Contest")
                .param("duration", "150")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Updated Contest"))
            .andExpect(jsonPath("$.duration").value(150))
    }

    @Test
    fun `update should return 404 when contest not found`() {
        whenever(contestService.update(eq(999L), any(), any())).thenReturn(null)

        mockMvc.perform(
            put("/api/contests/999")
                .param("name", "Updated Contest")
                .param("duration", "150")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `delete should return 204 when contest exists`() {
        doNothing().whenever(contestService).delete(1L)

        mockMvc.perform(delete("/api/contests/1"))
            .andExpect(status().isNoContent)

        verify(contestService, times(1)).delete(1L)
    }
}

