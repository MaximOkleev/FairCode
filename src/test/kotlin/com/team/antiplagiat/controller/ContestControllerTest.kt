package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.contest.ContestRequest
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
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import jakarta.servlet.http.HttpServletRequest
import com.team.antiplagiat.config.TokenPayload
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

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

    private fun setSecurityContext(userId: Long, role: String = "ADMIN") {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
        val auth = UsernamePasswordAuthenticationToken(userId, null, authorities)
        SecurityContextHolder.getContext().authentication = auth
    }

    private fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @BeforeEach
    fun setUp() {
        clearSecurityContext()
        reset(contestService, userService)
    }

    @Test
    fun `create should return 201 when contest created successfully`() {
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

        val contest = Contest(
            id = 1L,
            name = "Test Contest",
            admin = admin,
            startedAt = now,
            duration = 120
        )

        whenever(userService.findById(1L)).thenReturn(admin)
        whenever(contestService.create(any())).thenReturn(contest)

        // Set security context with ADMIN role
        setSecurityContext(userId = 1L, role = "ADMIN")

        mockMvc.perform(
            post("/api/contests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Contest"))
            .andExpect(jsonPath("$.duration").value(120))
    }

    @Test
    fun `create should return 401 when no token provided`() {
        val now = LocalDateTime.now()
        val request = ContestRequest(
            name = "Test Contest",
            startedAt = now,
            duration = 120
        )

        mockMvc.perform(
            post("/api/contests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `create should return 400 when admin not found`() {
        val now = LocalDateTime.now()
        val request = ContestRequest(
            name = "Test Contest",
            startedAt = now,
            duration = 120
        )

        whenever(userService.findById(1L)).thenReturn(null)

        mockMvc.perform(
            post("/api/contests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create should return 400 when contest service fails`() {
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

        whenever(userService.findById(1L)).thenReturn(admin)
        whenever(contestService.create(any())).thenReturn(null)

        mockMvc.perform(
            post("/api/contests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
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

        mockMvc.perform(get("/api/contests/1").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Contest"))
    }

    @Test
    fun `getById should return 404 when contest not found`() {
        whenever(contestService.findById(999L)).thenReturn(null)

        mockMvc.perform(get("/api/contests/999").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
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

        mockMvc.perform(get("/api/contests").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Contest 1"))
            .andExpect(jsonPath("$[1].duration").value(180))
    }

    @Test
    fun `getAll should return empty list when no contests`() {
        whenever(contestService.findAll()).thenReturn(emptyList())

        mockMvc.perform(get("/api/contests").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
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

        mockMvc.perform(get("/api/contests/by-admin").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `getByAdmin should return empty list when admin has no contests`() {
        whenever(contestService.findByAdmin(1L)).thenReturn(emptyList())

        mockMvc.perform(get("/api/contests/by-admin").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
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

        // Set security context with ADMIN role
        setSecurityContext(userId = 1L, role = "ADMIN")

        mockMvc.perform(
            put("/api/contests/1")
                .param("name", "Updated Contest")
                .param("duration", "150")
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
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
                .requestAttr("tokenPayload",TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `delete should return 204 when contest exists`() {
        doNothing().whenever(contestService).delete(1L)

        // Set security context with ADMIN role
        setSecurityContext(userId = 1L, role = "ADMIN")

        mockMvc.perform(delete("/api/contests/1").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isNoContent)

        verify(contestService, times(1)).delete(1L)
    }

    private fun requestWith(payload: TokenPayload?): HttpServletRequest = mockk<HttpServletRequest>().also {
        every { it.getAttribute("tokenPayload") } returns payload
    }

    private fun payload(userId: Long, role: String) = TokenPayload(
        userId = userId,
        login = "user$userId",
        email = "user$userId@example.com",
        role = role
    )

    @Test
    fun `contest controller covers unauthorized branches`() {
        val contestService = mockk<ContestService>(relaxed = true)
        val userService = mockk<UserService>(relaxed = true)
        val controller = ContestController(contestService, userService)
        val request = ContestRequest("Contest", LocalDateTime.now(), 60)

        assertEquals(HttpStatus.UNAUTHORIZED, controller.create(request, requestWith(null)).statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, controller.get(1L, requestWith(null)).statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, controller.getAll(requestWith(null)).statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, controller.getByAdmin(requestWith(null)).statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, controller.update(1L, "Updated", 120L, requestWith(null)).statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, controller.delete(1L, requestWith(null)).statusCode)
    }
}

