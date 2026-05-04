package com.team.antiplagiat.controller.dto

import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserDTOTest {

    @Test
    fun `UserRequest can be created with all fields`() {
        val request = UserRequest(
            login = "testuser",
            email = "test@example.com",
            role = User.Role.BASIC
        )

        assertEquals("testuser", request.login)
        assertEquals("test@example.com", request.email)
        assertEquals(User.Role.BASIC, request.role)
    }

    @Test
    fun `UserRequest with admin role`() {
        val request = UserRequest(
            login = "admin",
            email = "admin@example.com",
            role = User.Role.ADMIN
        )

        assertEquals(User.Role.ADMIN, request.role)
    }

    @Test
    fun `UserRequest toEntity creates user`() {
        val request = UserRequest(
            login = "newuser",
            email = "new@example.com",
            role = User.Role.ADMIN
        )

        val user = request.toEntity()

        assertEquals("newuser", user.login)
        assertEquals("new@example.com", user.email)
        assertEquals(User.Role.ADMIN, user.role)
        assertEquals(0L, user.id)
    }

    @Test
    fun `UserRequest toEntity with basic role`() {
        val request = UserRequest(
            login = "basicuser",
            email = "basic@example.com",
            role = User.Role.BASIC
        )

        val user = request.toEntity()

        assertEquals(User.Role.BASIC, user.role)
    }

    @Test
    fun `UserResponse can be created with all fields`() {
        val response = UserResponse(
            id = 5L,
            login = "responseuser",
            email = "response@example.com",
            role = User.Role.BASIC
        )

        assertEquals(5L, response.id)
        assertEquals("responseuser", response.login)
        assertEquals("response@example.com", response.email)
        assertEquals(User.Role.BASIC, response.role)
    }

    @Test
    fun `UserResponse fromEntity extracts correct data`() {
        val user = User(
            id = 10L,
            login = "entityuser",
            email = "entity@example.com",
            role = User.Role.ADMIN
        )

        val response = UserResponse.fromEntity(user)

        assertEquals(10L, response.id)
        assertEquals("entityuser", response.login)
        assertEquals("entity@example.com", response.email)
        assertEquals(User.Role.ADMIN, response.role)
    }

    @Test
    fun `UserResponse fromEntity with basic role`() {
        val user = User(
            id = 1L,
            login = "user1",
            email = "user1@example.com",
            role = User.Role.BASIC
        )

        val response = UserResponse.fromEntity(user)

        assertEquals(User.Role.BASIC, response.role)
    }

    @Test
    fun `UserRequest equality`() {
        val request1 = UserRequest("user", "user@example.com", User.Role.BASIC)
        val request2 = UserRequest("user", "user@example.com", User.Role.BASIC)

        assertEquals(request1, request2)
    }

    @Test
    fun `UserResponse equality`() {
        val response1 = UserResponse(1L, "user", "user@example.com", User.Role.BASIC)
        val response2 = UserResponse(1L, "user", "user@example.com", User.Role.BASIC)

        assertEquals(response1, response2)
    }

    @Test
    fun `UserRequest different roles`() {
        val basic = UserRequest("user", "user@example.com", User.Role.BASIC)
        val admin = UserRequest("user", "user@example.com", User.Role.ADMIN)

        assertEquals(false, basic == admin)
    }

    @Test
    fun `UserResponse different ids`() {
        val resp1 = UserResponse(1L, "user", "user@example.com", User.Role.BASIC)
        val resp2 = UserResponse(2L, "user", "user@example.com", User.Role.BASIC)

        assertEquals(false, resp1 == resp2)
    }
}

