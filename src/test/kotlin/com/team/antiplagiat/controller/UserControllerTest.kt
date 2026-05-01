package com.team.antiplagiat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.controller.dto.user.UserRequest
import com.team.antiplagiat.controller.dto.user.UserResponse
import com.team.antiplagiat.models.User
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
    fun `create should return 201 when user created successfully`() {
        val request = UserRequest(
            login = "testuser",
            email = "test@example.com",
            role = User.Role.BASIC
        )

        val user = User(
            id = 1L,
            login = "testuser",
            email = "test@example.com",
            role = User.Role.BASIC
        )

        whenever(userService.create(any())).thenReturn(user)

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.login").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.role").value("BASIC"))
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

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.login").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.role").value("BASIC"))
    }

    @Test
    fun `getById should return 404 when user not found`() {
        whenever(userService.findById(999L)).thenReturn(null)

        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getAll should return list of users`() {
        val users = listOf(
            User(id = 1L, login = "user1", email = "user1@example.com", role = User.Role.BASIC),
            User(id = 2L, login = "user2", email = "user2@example.com", role = User.Role.ADMIN)
        )

        whenever(userService.findAll()).thenReturn(users)

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].login").value("user1"))
            .andExpect(jsonPath("$[1].role").value("ADMIN"))
    }

    @Test
    fun `getAll should return empty list when no users`() {
        whenever(userService.findAll()).thenReturn(emptyList())

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `update should return 200 when user updated successfully`() {
        val request = UserRequest(
            login = "updateduser",
            email = "updated@example.com",
            role = User.Role.ADMIN
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
            role = User.Role.BASIC
        )

        whenever(userService.update(eq(999L), any(), any())).thenReturn(null)

        mockMvc.perform(
            put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `delete should return 204 when user exists`() {
        doNothing().whenever(userService).delete(1L)

        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isNoContent)

        verify(userService, times(1)).delete(1L)
    }
}

