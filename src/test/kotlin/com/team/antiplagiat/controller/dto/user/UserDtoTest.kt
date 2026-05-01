package com.team.antiplagiat.controller.dto.user

import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class UserDtoTest {

    @Test
    fun `UserRequest toEntity should create User correctly`() {
        val request = UserRequest(
            login = "testuser",
            email = "test@example.com",
            role = User.Role.BASIC
        )

        val user = request.toEntity()

        assertEquals("testuser", user.login)
        assertEquals("test@example.com", user.email)
        assertEquals(User.Role.BASIC, user.role)
        assertEquals(0L, user.id)
    }

    @Test
    fun `UserResponse fromEntity should convert User correctly`() {
        val user = User(
            id = 1L,
            login = "testuser",
            email = "test@example.com",
            role = User.Role.ADMIN
        )

        val response = UserResponse.fromEntity(user)

        assertEquals(1L, response.id)
        assertEquals("testuser", response.login)
        assertEquals("test@example.com", response.email)
        assertEquals(User.Role.ADMIN, response.role)
    }

    @Test
    fun `UserResponse should handle both User roles`() {
        val basicUserResponse = UserResponse.fromEntity(
            User(id = 1L, login = "basic", email = "basic@test.com", role = User.Role.BASIC)
        )
        val adminUserResponse = UserResponse.fromEntity(
            User(id = 2L, login = "admin", email = "admin@test.com", role = User.Role.ADMIN)
        )

        assertEquals(User.Role.BASIC, basicUserResponse.role)
        assertEquals(User.Role.ADMIN, adminUserResponse.role)
    }
}

