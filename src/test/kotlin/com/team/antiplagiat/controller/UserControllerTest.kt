package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.user.UserRequest
import com.team.antiplagiat.models.User
import com.team.antiplagiat.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import jakarta.servlet.http.HttpServletRequest
import com.team.antiplagiat.config.TokenPayload

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        reset(userService)
    }

    @Test
    fun `getById should return 200 when user exists`() {
        val user = User(
            id = 1L,
            login = "testuser",
            email = "test@example.com",
            role = User.Role.BASIC
        )

        whenever(userService.findById(1L)).thenReturn(user)

        mockMvc.perform(get("/api/users/1").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "testuser",
            email = "test@example.com",
            role = "BASIC"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.login").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.role").value("BASIC"))
    }

    @Test
    fun `getById should return 404 when user not found`() {
        whenever(userService.findById(999L)).thenReturn(null)

        mockMvc.perform(get("/api/users/999").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getAll should return list of users`() {
        val users = listOf(
            User(id = 1L, login = "user1", email = "user1@example.com", role = User.Role.BASIC),
            User(id = 2L, login = "user2", email = "user2@example.com", role = User.Role.ADMIN)
        )

        whenever(userService.findAll()).thenReturn(users)

        mockMvc.perform(get("/api/users").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].login").value("user1"))
            .andExpect(jsonPath("$[1].role").value("ADMIN"))
    }

    @Test
    fun `getAll should return 401 when no token provided`() {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `getAll should return empty list when no users`() {
        whenever(userService.findAll()).thenReturn(emptyList())

        mockMvc.perform(get("/api/users").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "admin",
            email = "admin@example.com",
            role = "ADMIN"
        )))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `update should return 200 when user updated successfully`() {
        val request = UserRequest(
            login = "updateduser",
            email = "updated@example.com",
        )

        val updatedUser = User(
            id = 1L,
            login = "updateduser",
            email = "updated@example.com",
            role = User.Role.ADMIN
        )

        whenever(userService.update(eq(1L), eq("updateduser"), eq("updated@example.com"))).thenReturn(updatedUser)

        mockMvc.perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "updateduser",
                    email = "updated@example.com",
                    role = "ADMIN"
                ))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.login").value("updateduser"))
            .andExpect(jsonPath("$.email").value("updated@example.com"))
    }

    @Test
    fun `update should return 404 when user not found`() {
        val request = UserRequest(
            login = "testuser",
            email = "test@example.com",
        )

        whenever(userService.update(eq(999L), any(), any())).thenReturn(null)

        mockMvc.perform(
            put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "admin",
                    email = "admin@example.com",
                    role = "ADMIN"
                ))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `update should return 401 when token missing`() {
        val request = UserRequest("newlogin", "new@example.com")

        mockMvc.perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `delete should return 204 when user exists`() {
        doNothing().whenever(userService).delete(1L)

        mockMvc.perform(delete("/api/users/1").requestAttr("tokenPayload", TokenPayload(
            userId = 1L,
            login = "testuser",
            email = "test@example.com",
            role = "BASIC"
        )))
            .andExpect(status().isNoContent)

        verify(userService, times(1)).delete(1L)
    }

    @Test
    fun `delete should return 401 when token missing`() {
        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `update should return 400 when validation fails - invalid email`() {
        val request = UserRequest("newlogin", "invalid-email")

        mockMvc.perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "user",
                    email = "user@example.com",
                    role = "BASIC"
                ))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `update should return 400 when validation fails - short login`() {
        val request = UserRequest("ab", "new@example.com")

        mockMvc.perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "user",
                    email = "user@example.com",
                    role = "BASIC"
                ))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `delete should return 403 when user tries to delete another user`() {
        mockMvc.perform(
            delete("/api/users/2")
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "user1",
                    email = "user1@example.com",
                    role = "BASIC"
                ))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `update should return 403 when user tries to update another user`() {
        val request = UserRequest("newlogin", "new@example.com")

        mockMvc.perform(
            put("/api/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "user1",
                    email = "user1@example.com",
                    role = "BASIC"
                ))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `get should return 403 when user tries to get another user and is not admin`() {
        val user = User(
            id = 2L,
            login = "otheruser",
            email = "other@example.com",
            role = User.Role.BASIC
        )

        whenever(userService.findById(2L)).thenReturn(user)

        mockMvc.perform(
            get("/api/users/2")
                .requestAttr("tokenPayload", TokenPayload(
                    userId = 1L,
                    login = "user1",
                    email = "user1@example.com",
                    role = "BASIC"
                ))
        )
            .andExpect(status().isForbidden)
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
    fun `user controller covers unauthorized and forbidden branches`() {
        val service = mockk<UserService>(relaxed = true)
        val controller = UserController(service)

        assertEquals(HttpStatus.UNAUTHORIZED, controller.get(1L, requestWith(null)).statusCode)
        assertEquals(HttpStatus.FORBIDDEN, controller.get(2L, requestWith(payload(1L, "BASIC"))).statusCode)
        assertEquals(HttpStatus.FORBIDDEN, controller.getAll(requestWith(payload(1L, "BASIC"))).statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, controller.update(1L, UserRequest("x", "x@e.com"), requestWith(null)).statusCode)
        assertEquals(HttpStatus.FORBIDDEN, controller.update(2L, UserRequest("x", "x@e.com"), requestWith(payload(1L, "BASIC"))).statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, controller.delete(1L, requestWith(null)).statusCode)
        assertEquals(HttpStatus.FORBIDDEN, controller.delete(2L, requestWith(payload(1L, "BASIC"))).statusCode)
    }
}
