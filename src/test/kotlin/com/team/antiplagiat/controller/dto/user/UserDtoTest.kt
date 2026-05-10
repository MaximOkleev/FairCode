package com.team.antiplagiat.controller.dto.user

import com.team.antiplagiat.models.User
import com.team.antiplagiat.controller.dto.UserRequest as RootUserRequest
import com.team.antiplagiat.controller.dto.UserResponse as RootUserResponse
import com.team.antiplagiat.controller.dto.toEntity as toRootEntity
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

    @Test
    fun `root UserRequest toEntity should create User correctly`() {
        val request = RootUserRequest(
            login = "root_user",
            email = "root@example.com",
            role = User.Role.ADMIN
        )

        val user = request.toRootEntity()

        assertEquals("root_user", user.login)
        assertEquals("root@example.com", user.email)
        assertEquals(User.Role.ADMIN, user.role)
        assertEquals(0L, user.id)
    }

    @Test
    fun `root UserResponse fromEntity should convert User correctly`() {
        val user = User(id = 42L, login = "root_resp", email = "resp@example.com", role = User.Role.BASIC)

        val response = RootUserResponse.fromEntity(user)

        assertEquals(42L, response.id)
        assertEquals("root_resp", response.login)
        assertEquals("resp@example.com", response.email)
        assertEquals(User.Role.BASIC, response.role)
    }

    @Test
    fun `user dto mapping`() {
        val user = User(id = 2L, login = "u1", email = "u1@e.com", role = User.Role.BASIC)
        val resp = RootUserResponse.fromEntity(user)
        assertEquals(2L, resp.id)
        assertEquals("u1", resp.login)

        val req = RootUserRequest(login = "x", email = "x@e.com", role = User.Role.BASIC)
        assertEquals("x", req.login)
    }

    @Test
    fun `user dto round trip`() {
        val request = RootUserRequest(login = "u", email = "u@example.com", role = User.Role.BASIC)
        val entity = request.toRootEntity()
        val response = RootUserResponse.fromEntity(entity.apply { id = 20L })

        assertEquals("u", entity.login)
        assertEquals("u@example.com", entity.email)
        assertEquals(User.Role.BASIC, entity.role)
        assertEquals(20L, response.id)
        assertEquals("u", response.login)
    }
}

